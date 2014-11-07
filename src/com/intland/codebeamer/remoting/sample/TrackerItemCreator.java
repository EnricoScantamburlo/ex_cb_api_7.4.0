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

import com.intland.codebeamer.persistence.dto.ProjectDto;
import com.intland.codebeamer.persistence.dto.TrackerDto;
import com.intland.codebeamer.persistence.dto.TrackerItemDto;
import com.intland.codebeamer.remoting.RemoteApi;
import com.intland.codebeamer.remoting.RemoteApiFactory;

/**
 * @author <a href="mailto:zsolt.koppany@intland.com">Zsolt Koppany</a>
 * @version $Id$
 */
public class TrackerItemCreator {
	public static void main(String[] args) throws Exception {
		final String serviceUrl = args[0];
		final String login = args[1];
		final String password = args[2];
		final String projectName = args[3];

		System.out.println("Connecting to CodeBeamer web service at " + serviceUrl + "...");
		RemoteApi api = RemoteApiFactory.getInstance().connect(serviceUrl);
		if(api == null) {
			System.err.println("Couldn't connect, is the service URL correct?");
			System.exit(-1);
		}

		System.out.println("Signing in...");
		String token = api.login(login, password);

		TrackerDto tracker = findTracker(api, token, projectName, "Bug");
		if (tracker != null) {
			createTrackerItem(api, token, tracker);
		}
	}

	private static TrackerDto findTracker(RemoteApi api, String token, String projectName, String trackerName) {
		ProjectDto project = api.findProjectByName(token, projectName);
		if (project == null) {
			return null;
		}

		TrackerDto[] trackers = api.findTrackersByProject(token, project.getId());
		for (int i = 0; i < trackers.length; i++) {
			TrackerDto tracker = trackers[i];
			if (tracker.getName().equalsIgnoreCase(trackerName)) {
				System.out.println("Found tracker: " + tracker.getId() + " project-id: " + tracker.getProject().getId());
				return tracker;
			}
		}
		return null;
	}

	private static void createTrackerItem(RemoteApi api, String token, TrackerDto tracker) throws Exception {
		TrackerItemDto trackerItem = new TrackerItemDto();
		trackerItem.setTracker(tracker);
		trackerItem.setName("Hello World");
		trackerItem.setDescription("this is my wiki __BOLD__ description");
		trackerItem.setDescriptionFormat("W");

		TrackerItemDto newTrackerItem = api.createTrackerItem(token, trackerItem);

		System.out.println("Tracker item created ID: " + newTrackerItem.getId());

		newTrackerItem.setName(newTrackerItem.getName() + " ---");
		api.updateTrackerItem(token, newTrackerItem);
	}
}
