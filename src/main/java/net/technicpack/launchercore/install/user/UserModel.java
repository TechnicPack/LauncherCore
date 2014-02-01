package net.technicpack.launchercore.install.user;

import net.technicpack.launchercore.auth.AuthResponse;
import net.technicpack.launchercore.auth.AuthenticationService;
import net.technicpack.launchercore.exception.AuthenticationNetworkFailureException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class UserModel {

	private User mCurrentUser = null;
	private List<IAuthListener> mAuthListeners = new LinkedList<>();
	private IUserStore mUserStore;

	public UserModel(IUserStore userStore) {
		this.mCurrentUser = null;
		this.mUserStore = userStore;
	}

	public User getCurrentUser() {
		return this.mCurrentUser;
	}

	public void setCurrentUser(User user) {
		this.mCurrentUser = user;
		this.triggerAuthListeners();
	}

	public void addAuthListener(IAuthListener listener) {
		this.mAuthListeners.add(listener);
	}

	protected void triggerAuthListeners() {
		for(IAuthListener listener : this.mAuthListeners) {
			listener.userChanged(this.mCurrentUser);
		}
	}

	public AuthError AttemptLastUserRefresh() throws AuthenticationNetworkFailureException {
		String lastUser = this.mUserStore.getLastUser();

		if (lastUser == null || lastUser.isEmpty()) {
			return new AuthError("No cached user", "Could not log into the last logged in user, as there was no cached user to log into.");
		}

		User user = this.mUserStore.getUser(lastUser);

		if (user == null) {
			return new AuthError("No cached user", "Could not log into the specified user, as there was no cached user to log into.");
		}

		return AttemptUserRefresh(user);
	}

	public AuthError AttemptUserRefresh(User userparam) throws AuthenticationNetworkFailureException {
		User user = userparam;
		AuthResponse response = AuthenticationService.requestRefresh(user);
		if (response.getError() != null) {
			this.mUserStore.removeUser(user.getUsername());
			return new AuthError(response.getError(), response.getErrorMessage());
		}
		//Refresh user from response
		user = new User(user.getUsername(), response);
		this.mUserStore.addUser(user);
		setCurrentUser(user);
		return null;
	}

	public Collection<User> getUsers() {
		return this.mUserStore.getSavedUsers();
	}

	public User getLastUser() {
		return this.mUserStore.getUser(this.mUserStore.getLastUser());
	}

	public User getUser(String username) {
		return this.mUserStore.getUser(username);
	}

	public void addUser(User user) {
		this.mUserStore.addUser(user);
	}

	public void removeUser(User user) {
		this.mUserStore.removeUser(user.getUsername());
	}

	public void setLastUser(User user) {
		this.mUserStore.setLastUser(user.getUsername());
	}

	public String getClientToken() {
		return this.mUserStore.getClientToken();
	}

	public class AuthError {
		private String mError;
		private String mErrorDescription;

		public AuthError(String error, String errorDescription) {
			this.mError = error;
			this.mErrorDescription = errorDescription;
		}

		public String getError() {
			return this.mError;
		}

		public String getErrorDescription() {
			return this.mErrorDescription;
		}
	}
}
