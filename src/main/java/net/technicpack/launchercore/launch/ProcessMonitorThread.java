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

package net.technicpack.launchercore.launch;

import net.technicpack.launchercore.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessMonitorThread extends Thread {

	private final MinecraftProcess process;
	protected boolean hidden;

	public ProcessMonitorThread(MinecraftProcess process) {
		super("ProcessMonitorThread");
		this.process = process;
	}

	@Override
	public void run() {
		InputStreamReader reader = new InputStreamReader(this.process.getProcess().getInputStream());
		BufferedReader buf = new BufferedReader(reader);
		String line = null;

		while (true) {
			try {
				while ((line = buf.readLine()) != null) {
					System.out.println(" " + line);
				}
			} catch (IOException ex) {
				//Do nothing
			} finally {
				try {
					buf.close();
				} catch (IOException ex) {
					//Do nothing
				} finally {
					if (process.getExitListener() != null && hidden == true) {
						process.getExitListener().onMinecraftExit(process);
						hidden = false;
					}
				}
			}
		}
	}
}
