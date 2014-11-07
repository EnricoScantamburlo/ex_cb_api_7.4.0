/*
 * Copyright by Intland Software
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Intland Software. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Intland.
 */
package com.intland.codebeamer.remoting.sample;

import java.text.NumberFormat;

import com.intland.codebeamer.persistence.dto.ArtifactDto;
import com.intland.codebeamer.persistence.dto.ProjectDto;
import com.intland.codebeamer.persistence.dto.TrackerDto;
import com.intland.codebeamer.persistence.dto.TrackerItemDto;
import com.intland.codebeamer.persistence.dto.UserDto;
import com.intland.codebeamer.remoting.RemoteApi;
import com.intland.codebeamer.remoting.RemoteApiFactory;
import com.intland.codebeamer.remoting.bean.ServerInfo;


/**
 * This program is part of the CodeBeamer SDK.
 * <p>
 * This console application measures remote API throughput mostly
 * using various finders.<br/>
 * You can use this as skeleton to develop your own
 * profilers.
 *
 * @author <a href="mailto:aron.gombas@intland.com">Aron Gombas</a>
 * @version $RCSfile$ $Revision$ $Date$
 */
public class CodeBeamerRemoteProfiler {
	private static NumberFormat formatter = NumberFormat.getInstance();

	/**
	 * Main entry point.
	 * Commandline parsing is not elaborated, for real use
	 * please consider using the Apache Commons CLI package.
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 3) {
			System.err.println("Usage: <program> service-URL (for example http://localhost:8080/cb/remote-api)\n"
				+ "login password");
			System.exit(-1);
		}
		String serviceUrl = args[0];
		String login = args[1];
		String password = args[2];

		System.out.println("Connecting to CodeBeamer web service at " + serviceUrl + "...");
		RemoteApi api = RemoteApiFactory.getInstance().connect(serviceUrl);
		if(api == null) {
			System.err.println("Couldn't connect, is the service URL correct?");
			System.exit(-1);
		}

		System.out.println("Signing in...");
		String token = api.login(login, password);
		ServerInfo serverInfo = api.getServerInfo();
		System.out.println("Signed in to CodeBeamer " + serverInfo.getMajorVersion() + serverInfo.getMinorVersion() + " (" + serverInfo.getBuildDate() + ") running on " + serverInfo.getOs() + "/Java " + serverInfo.getJavaVersion());

		profileUsers(api, token);
		profileProjects(api, token);
		profileArtifacts(api, token);
		profileTrackers(api, token);
		profileTrackerItems(api, token);

		System.out.println("Signing out...");
		api.logout(token);

		System.out.println("Done");
	}

	/**
	 * Profiles user account management performance.
	 */
	protected static void profileUsers(RemoteApi api, String token) {
		long startTime = System.currentTimeMillis();
		UserDto users[] = api.findAllUsers(token);
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);

		if(users != null)
			System.out.println("findAllUsers\": " + users.length + " users in " + duration + " ms, " + formatter.format(1000.0*users.length / duration) + "/s");
		else
			System.out.println("User information is not available");
	}

	/**
	 * Profiles project management performance.
	 */
	protected static void profileProjects(RemoteApi api, String token) {
		long startTime = System.currentTimeMillis();
		ProjectDto projects[] = api.findAllProjects(token);
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);

		System.out.println("findAllProjects\": " + projects.length + " projects in " + duration + " ms, " + formatter.format(1000.0*projects.length / duration) + "/s");
	}

	/**
	 * Profiles artifact management performance.
	 */
	protected static void profileArtifacts(RemoteApi api, String token) {
		ProjectDto projects[] = api.findAllProjects(token);
		for(int i = 0; i < projects.length; i++) {
			long startTime = System.currentTimeMillis();
			ArtifactDto artifacts[] = api.findTopArtifactsByProject(token, projects[i].getId());
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime);

			System.out.println("findTopArtifactsByProject\": " + artifacts.length + " artifacts in " + duration + " ms, " + formatter.format(1000.0*artifacts.length / duration) + "/s");

			for(int j = 0; j < artifacts.length; j++)
				profileArtifact(api, token, artifacts[j]);
		}
	}

	/**
	 * Profiles artifact management performance for a single artifact.
	 */
	protected static void profileArtifact(RemoteApi api, String token, ArtifactDto artifact) {
		// traverse children recursively
		if(artifact.isDirectory()) {
			long startTime = System.currentTimeMillis();
			ArtifactDto children[] = api.findArtifactsByParentArtifact(token, artifact.getId());
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime);

			System.out.println("findArtifactsByParentArtifact\": " + children.length + " artifacts in " + duration + " ms, " + formatter.format(1000.0*children.length / duration) + "/s");

			for(int i = 0; i < children.length; i++)
				profileArtifact(api, token, children[i]);
		}
	}

	/**
	 * Profiles tracker management performance.
	 */
	protected static void profileTrackers(RemoteApi api, String token) {
		long startTime = System.currentTimeMillis();
		TrackerDto trackers[] = api.findAllTrackers(token);
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);

		System.out.println("findAllTrackers\": " + trackers.length + " trackers in " + duration + " ms, " + formatter.format(1000.0*trackers.length / duration) + "/s");
	}

	/**
	 * Profiles tracker item management performance.
	 */
	protected static void profileTrackerItems(RemoteApi api, String token) {
		TrackerDto trackers[] = api.findAllTrackers(token);
		for(int i = 0; i < trackers.length; i++) {
			long startTime = System.currentTimeMillis();
			TrackerItemDto items[] = api.findTrackerItemsByTrackerId(token, trackers[i].getId());
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime);

			System.out.println("findTrackerItemsByTrackerId\": " + items.length + " items in " + duration + " ms, " + formatter.format(1000.0*items.length / duration) + "/s");
		}
	}
}
