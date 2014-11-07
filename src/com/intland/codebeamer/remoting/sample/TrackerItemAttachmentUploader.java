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


import com.intland.codebeamer.persistence.dto.ArtifactDto;
import com.intland.codebeamer.persistence.dto.BinaryStreamDto;
import com.intland.codebeamer.persistence.dto.ProjectDto;
import com.intland.codebeamer.persistence.dto.TrackerItemDto;
import com.intland.codebeamer.persistence.util.BinaryStreamDtoHelper;
import com.intland.codebeamer.remoting.RemoteApi;
import com.intland.codebeamer.remoting.RemoteApiFactory;
import com.intland.codebeamer.remoting.bean.ServerInfo;


/**
 * This program is part of the CodeBeamer SDK.
 * <p>
 * This console application illustrates:
 * <ul>
 *   <li>connecting to CodeBeamer</li>
 *   <li>signing in as a CodeBeamer user</li>
 *   <li>creating a new folder in the root dir of the project</li>
 *   <li>uploading a new textfile as to this folder with the status "New"</li>
 *   <li>adding a second revision of the same textfile</li>
 * </ul>
 *
 * @author <a href="mailto:aron.gombas@intland.com">Aron Gombas</a>
 * @version $Id: zsolt 2009-11-27 19:54 +0100 23955:cdecf078ce1f  $
 */
public class TrackerItemAttachmentUploader {
	/** Main entry point. */
	public static void main(String[] args) throws Exception {
		if(args.length != 4) {
			System.err.println("Usage: <program> service-URL login password project-name");
			System.exit(-1);
		}
		String serviceUrl = args[0];
		String login = args[1];
		String password = args[2];
		String projectName = args[3];

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

		System.out.println("Searching for project \"" + projectName + "\"...");
		ProjectDto project = findProjectByName(api.findAllProjects(token), projectName);
		if(project != null) {

			// TODO Add tracker item creation code
			TrackerItemDto trackerItem = null;

			ArtifactDto attachment = new ArtifactDto();
			attachment.setName("myfilename");
			attachment.setMimeType("text/plain");
			attachment.setDescription("mydescription");

			BinaryStreamDto content = BinaryStreamDtoHelper.createFromBytes("thedata".getBytes());

			trackerItem = api.addUpdTrackerItemAttachments(token, trackerItem.getId(), attachment, new BinaryStreamDto[]{content}, null);
		} else {
			System.out.println("Couldn't find project \"" + projectName + "\"");
		}

		System.out.println("Signing out...");
		api.logout(token);

		System.out.println("Done");
	}

	/** Returns the project with the given name. */
	protected static ProjectDto findProjectByName(ProjectDto projects[], String projectName) {
		for(int i = 0; i < projects.length; i++)
			if(projects[i].getName().equals(projectName))
				return projects[i];

		return null;
	}
}
