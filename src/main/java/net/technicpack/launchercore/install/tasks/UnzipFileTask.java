package net.technicpack.launchercore.install.tasks;

import net.technicpack.launchercore.util.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

public class UnzipFileTask extends ListenerTask {
	private File zipFile;
	private File destination;

	public UnzipFileTask(File zipFile, File destination) {
		this.zipFile = zipFile;
		this.destination = destination;
	}

	@Override
	public String getTaskDescription() {
		return "Unzipping "+this.zipFile.getName();
	}

	@Override
	public void runTask(InstallTasksQueue queue) throws IOException {
		super.runTask(queue);

		if (!this.zipFile.exists()) {
			throw new ZipException("Attempting to extract file "+this.zipFile.getName()+", but it did not exist.");
		}

		if (!this.destination.exists()) {
			this.destination.mkdirs();
		}

		ZipUtils.unzipFile(this.zipFile, this.destination, this);
	}
}
