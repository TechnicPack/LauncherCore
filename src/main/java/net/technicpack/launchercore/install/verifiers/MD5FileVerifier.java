/*
 * This file is part of Technic Launcher Core.
 * Copyright (C) 2013 Syndicate, LLC
 *
 * Technic Launcher Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Technic Launcher Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * as well as a copy of the GNU Lesser General Public License,
 * along with Technic Launcher Core.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.technicpack.launchercore.install.verifiers;

<<<<<<< HEAD:src/main/java/net/technicpack/launchercore/install/verifiers/MD5FileVerifier.java
import net.technicpack.utilslib.MD5Utils;
=======
public class Profile {
    private String id;
    private String name;
    private boolean legacy;
>>>>>>> Re-tab & optimize imports for all files in core.:src/main/java/net/technicpack/launchercore/auth/Profile.java

import java.io.File;

public class MD5FileVerifier implements IFileVerifier {
    private String md5Hash;

    public MD5FileVerifier(String md5Hash) {
        this.md5Hash = md5Hash;
    }

<<<<<<< HEAD:src/main/java/net/technicpack/launchercore/install/verifiers/MD5FileVerifier.java
    public boolean isFileValid(File file) {
        if (md5Hash == null || md5Hash.isEmpty())
            return false;

        String resultMD5 = MD5Utils.getMD5(file);

        System.out.println("Expected MD5: " + md5Hash + " Calculated MD5: " + resultMD5);
        return (md5Hash.equalsIgnoreCase(resultMD5));
=======
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isLegacy() {
        return legacy;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
>>>>>>> Re-tab & optimize imports for all files in core.:src/main/java/net/technicpack/launchercore/auth/Profile.java
    }
}
