package net.technicpack.launchercore.install.tasks;

import net.technicpack.launchercore.minecraft.CompleteVersion;
import net.technicpack.launchercore.util.DownloadListener;
import java.io.IOException;
import java.util.LinkedList;

public class InstallTasksQueue {
	private DownloadListener listener;
	private LinkedList<IInstallTask> tasks;
	private IInstallTask currentTask;
	private CompleteVersion completeVersion;

	public InstallTasksQueue(DownloadListener listener) {
		this.listener = listener;
		this.tasks = new LinkedList<>();
		this.currentTask = null;
	}

	public void RefreshProgress() {
		this.listener.stateChanged(this.currentTask.getTaskDescription(), this.currentTask.getTaskProgress());
	}

	public void RunAllTasks() throws IOException {
		while (!this.tasks.isEmpty()) {
			this.currentTask = this.tasks.removeFirst();
			RefreshProgress();
			this.currentTask.runTask(this);
		}
	}

	public void AddNextTask(IInstallTask task) {
		this.tasks.addFirst(task);
	}

	public void AddTask(IInstallTask task) {
		this.tasks.addLast(task);
	}

	public void setCompleteVersion(CompleteVersion version) {
		this.completeVersion = version;
	}

	public CompleteVersion getCompleteVersion() {
		return this.completeVersion;
	}
}
