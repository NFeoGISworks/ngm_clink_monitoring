/*******************************************************************************
 * Project:  NextGIS mobile apps for Compulink
 * Purpose:  Mobile GIS for Android
 * Authors:  Dmitry Baryshnikov (aka Bishop), polimax@mail.ru
 *           NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (C) 2014-2015 NextGIS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package ar.com.daidalos.afiledialog;

import android.os.Environment;
import android.support.v4.util.Pair;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


// based on from http://stackoverflow.com/a/19982338/4727406, author Vitaliy Polchuk
// additions from http://stackoverflow.com/a/13648873/4727406, author Gnathonic
public class StorageUtils
{
    private static final String TAG = "StorageUtils";


    public static class StorageInfo
    {
        public final Pair<String, String> device;
        public final String               mountPointPath;
        public final String               mountDirName;
        public final boolean              primary;
        public final boolean              readonly;


        StorageInfo(
                Pair<String, String> device,
                String mountPointPath,
                String mountDirName,
                boolean primary,
                boolean readonly)
        {
            this.device = device;
            this.mountPointPath = mountPointPath;
            this.mountDirName = mountDirName;
            this.primary = primary;
            this.readonly = readonly;
        }


        private StringBuilder getNameMainPart()
        {
            StringBuilder res = new StringBuilder();

            if (primary) {
                res.append("Primary SD Card");
            } else {
                res.append("SD Card");
            }

            res.append(" [");
            res.append(mountDirName);
            res.append("]");

            return res;
        }


        public String getDisplayName()
        {
            StringBuilder res = getNameMainPart();

            if (readonly) {
                res.append(" (Read only)");
            }

            return res.toString();
        }


        public String getHtmlFormattedDisplayName()
        {
            StringBuilder res = getNameMainPart();

            if (readonly) {
                res.append("<br><small> (Read only)</small>");
            }

            return res.toString();
        }
    }


    public static List<StorageUtils.StorageInfo> getStorageList()
    {
        Set<Pair<String, String>> mountDevs = new HashSet<>();
        List<StorageInfo> storageInfos = new ArrayList<>();

        String primaryMountPoint = Environment.getExternalStorageDirectory().getPath();

        BufferedReader bufReader = null;
        String regDevices = ".*(/dev/block/vold|/dev/fuse).*";
        String regBadMountPoints = ".*(secure|asec|obb).*";
        String regFileSystems = ".*(vfat|ntfs|exfat|fat32|ext3|ext4|fuse).*";

        try {
            bufReader = new BufferedReader(new FileReader("/proc/self/mountinfo"));
            Log.d(TAG, "/proc/self/mountinfo");

            String line;
            while ((line = bufReader.readLine()) != null) {
                Log.d(TAG, line);

                List<String> columns = Arrays.asList(line.split(" "));

                // see https://www.kernel.org/doc/Documentation/filesystems/proc.txt
                // the part  "3.5  /proc/<pid>/mountinfo - Information about mounts"

                // mount ID:  unique identifier of the mount (may be reused after umount)
                //columns.get(0);

                // parent ID:  ID of parent (or of self for the top of the mount tree)
                //columns.get(1);

                // major:minor:  value of st_dev for files on filesystem
                String majorMinor = columns.get(2);

                // root:  root of the mount within the filesystem
                String rootOfMount = columns.get(3);

                // mount point:  mount point relative to the process's root
                String mountPoint = columns.get(4);

                // mount options:  per mount options
                String mountOptions = columns.get(5);

                // optional fields:  zero or more fields of the form "tag[:value]"
                int i = 6;
                for (; i < columns.size(); ++i) {
                    // separator:  marks the end of the optional fields
                    if (columns.get(i).equals("-")) {
                        break;
                    }
                }

                // filesystem type:  name of filesystem of the form "type[.subtype]"
                String filesystemType = null;
                if (i + 1 < columns.size()) {
                    filesystemType = columns.get(i + 1);
                }

                // mount source:  filesystem specific information or "none"
                String mountSource = null;
                if (i + 2 < columns.size()) {
                    mountSource = columns.get(i + 2);
                    if (mountSource.equals("none")) {
                        mountSource = null;
                    }
                }

                // super options:  per super block options
                //if (i + 3 < columns.size()) {
                //    columns.get(i + 3);
                //}


                // mount point
                if (mountPoint.matches(regBadMountPoints)) {
                    continue;
                }

                // device
                if (null == mountSource || !mountSource.matches(regDevices)) {
                    continue;
                }

                // file system
                if (null == filesystemType || !filesystemType.matches(regFileSystems)) {
                    continue;
                }

                // mount flags
                List<String> flags = Arrays.asList(mountOptions.split(","));

                boolean writable = flags.contains("rw");
                boolean readonly = flags.contains("ro");

                if (!writable && !readonly) {
                    continue;
                }

                File mountDir = new File(mountPoint);

                if (!mountDir.exists() || !mountDir.isDirectory() || !mountDir.canWrite()) {
                    continue;
                }

                Pair<String, String> device = Pair.create(majorMinor, rootOfMount);
                String mountDirName = mountDir.getName();
                boolean primary = mountPoint.equals(primaryMountPoint);

                if (primary && mountDevs.contains(device)) {

                    for (Iterator<StorageInfo> iterator = storageInfos.iterator();
                         iterator.hasNext(); ) {

                        StorageInfo info = iterator.next();

                        if (info.device.equals(device)) {
                            iterator.remove();
                            storageInfos.add(
                                    new StorageInfo(
                                            device, mountPoint, mountDirName, true, readonly));
                            break;
                        }
                    }
                }

                if (mountDevs.contains(device)) {
                    continue;
                }

                mountDevs.add(Pair.create(majorMinor, rootOfMount));
                storageInfos.add(
                        new StorageInfo(device, mountPoint, mountDirName, primary, readonly));
            }


        } catch (IOException ex) {
            Log.d(TAG, ex.getLocalizedMessage());

        } finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException ignored) {
                }
            }
        }

        mountDevs.clear();
        mountDevs = null;

        Collections.sort(
                storageInfos, new Comparator<StorageInfo>()
                {
                    public int compare(
                            StorageInfo info1,
                            StorageInfo info2)
                    {
                        if (info1 == null && info2 == null) {
                            return 0;
                        }
                        if (info1 == null) {
                            return -1;
                        }
                        if (info2 == null) {
                            return 1;
                        }
                        if (info1.mountDirName == null && info2.mountDirName == null) {
                            return 0;
                        }
                        if (info1.mountDirName == null) {
                            return -1;
                        }
                        if (info2.mountDirName == null) {
                            return 1;
                        }
                        return info1.mountDirName.compareTo(info2.mountDirName);
                    }
                });

        return storageInfos;
    }
}
