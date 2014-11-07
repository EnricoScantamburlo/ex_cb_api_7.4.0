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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.Ostermiller.util.CSVPrinter;
import com.intland.codebeamer.persistence.dto.ArtifactDto;
import com.intland.codebeamer.persistence.dto.ProjectDto;
import com.intland.codebeamer.persistence.dto.TrackerChoiceOptionDto;
import com.intland.codebeamer.persistence.dto.TrackerDto;
import com.intland.codebeamer.persistence.dto.TrackerItemDto;
import com.intland.codebeamer.persistence.dto.UserDto;
import com.intland.codebeamer.persistence.dto.base.IdentifiableDto;
import com.intland.codebeamer.persistence.dto.base.NamedDto;
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
 *   <li>accessing various data from CodeBeamer</li>
 *   <li>exporting the data to CSV files for future use</li>
 * </ul>
 * You can use this as skeleton to develop your own
 * exporters.
 *
 * @see com.intland.codebeamer.remoting.sample.CodeBeamerCsvImporter
 *
 * @author <a href="mailto:aron.gombas@intland.com">Aron Gombas</a>
 * @version $RCSfile$ $Revision: 23443:3b63eb1c80e4 $ $Date: 2009-10-29 17:28 +0100 $
 */
public class CodeBeamerCsvExporter {
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	/**
	 * Main entry point.
	 * Commandline parsing is not elaborated, for real use
	 * please consider using the Apache Commons CLI package.
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 8) {
			System.err.println("Usage: <program> service-URL (for example http://localhost:8080/cb/remote-api)\n"
				+ "login password projects.csv-path "
				+ "artifacts.csv-path trackers.csv-path trackeritems.csv-path useritems.csv-path [user.csv-path]");
			System.exit(-1);
		}
		String serviceUrl = args[0];
		String login = args[1];
		String password = args[2];
		String projectCsvPath = args[3];
		String artifactCsvPath = args[4];
		String trackerCsvPath = args[5];
		String trackerItemCsvPath = args[6];
		String trackerUserItemCsvPath = args[7];
		String userCsvPath = args.length > 8 ? args[8] : null;

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

		if (userCsvPath != null) {
			System.out.print("Exporting users...");
			System.out.println(" " + exportUsers(api, token, new FileOutputStream(userCsvPath)));
		}

		System.out.print("Exporting projects...");
		System.out.println(" " + exportProjects(api, token, new FileOutputStream(projectCsvPath)));

		System.out.print("Exporting artifacts...");
		System.out.println(" " + exportArtifacts(api, token, new FileOutputStream(artifactCsvPath)));

		System.out.print("Exporting trackers...");
		System.out.println(" " + exportTrackers(api, token, new FileOutputStream(trackerCsvPath)));

		System.out.print("Exporting tracker items...");
		System.out.println(" " + exportTrackerItems(api, token, new FileOutputStream(trackerItemCsvPath)));

		System.out.print("Exporting user-tracker items...");
		System.out.println(" " + exportUserTrackerItems(api, token, new FileOutputStream(trackerUserItemCsvPath)));

		System.out.println("Signing out...");
		api.logout(token);

		System.out.println("Done");
	}

	/**
	 * Exports all user account information that the user has access to.
	 * @return the number of user accounts exported.
	 */
	protected static int exportUsers(RemoteApi api, String token, OutputStream out) {
		int exported = 0;

		try {
			CSVPrinter printer = new CSVPrinter(out);
			printer.writeln(new String[] {
				"id",
				"name",
				"status",
				"hostName",
				"firstName",
				"lastName",
				"title",
				"address",
				"zip",
				"city",
				"state",
				"sourceOfInterest",
				"scc",
				"teamSize",
				"divisionSize",
				"company",
				"country",
				"email",
				"emailClient",
				"phone",
				"mobile",
				"dateFormatPattern",
				"dateTimeFormatPattern",
				"timeZonePattern",
				"downloadLimit",
				"browser",
				"skills",
				"registryDate",
				"lastLogin"});

			UserDto users[] = api.findAllUsers(token);
			if (users == null) {
				System.err.println("Couldn't get accounts!");
				return 0;
			}

			for(int i = 0; i < users.length; i++) {
				UserDto user = users[i];
				printer.writeln(new String[] {
					writeInteger(user.getId()),
					user.getName(),
					user.getStatus(),
					user.getHostName(),
					user.getFirstName(),
					user.getLastName(),
					user.getTitle(),
					user.getAddress(),
					user.getZip(),
					user.getCity(),
					user.getState(),
					user.getSourceOfInterest(),
					user.getScc(),
					user.getTeamSize(),
					user.getDivisionSize(),
					user.getCompany(),
					user.getCountry(),
					user.getEmail(),
					user.getEmailClient(),
					user.getPhone(),
					user.getMobile(),
					user.getDateFormatPattern(),
					user.getDateTimeFormatPattern(),
					user.getTimeZonePattern(),
					Integer.toString(user.getDownloadLimit()),
					user.getBrowser(),
					user.getSkills(),
					writeDate(user.getRegistryDate()),
					writeDate(user.getLastLogin()) });

				exported++;
			}
		} catch(IOException ex) {
			System.err.println(ex);
		}

		return exported;
	}

	/**
	 * Exports all project information that the user has access to.
	 * @return the number of projects exported.
	 */
	protected static int exportProjects(RemoteApi api, String token, OutputStream out) {
		int exported = 0;

		try {
			CSVPrinter printer = new CSVPrinter(out);
			printer.writeln(new String[] {
				"id",
				"name",
				"description",
				"descriptionFormat",
				"propagation",
				"defaultMemberRoleId",
				"allowedHost",
				"userName",
				"password",
				"homePage",
				"syncOptions",
				"startDate",
				"endDate",
				"createdAt",
				"createdBy",
				"createdFromHost",
				"virtualHost",
				"environment",
				"category",
				"copyright",
				"natureLanguage",
				"developmentLanguage",
				"status"});

			ProjectDto projects[] = api.findAllProjects(token);
			for(int i = 0; i < projects.length; i++) {
				ProjectDto project = projects[i];
				printer.writeln(new String[] {
					writeInteger(project.getId()),
					project.getName(),
					project.getDescription(),
					project.getDescriptionFormat(),
					project.getPropagation(),
					writeInteger(project.getDefaultMemberRoleId()),
					project.getAllowedHost(),
					project.getUserName(),
					project.getPassword(),
					writeDate(project.getStartDate()),
					writeDate(project.getEndDate()),
					writeDate(project.getCreatedAt()),
					writeUser(project.getCreatedBy()),
					project.getCreatedFromHost(),
					project.getVirtualHost(),
					project.getEnvironment(),
					project.getCategory(),
					project.getCopyright(),
					project.getNatureLanguage(),
					project.getDevelopmentLanguage(),
					project.getStatus()});

				exported++;
			}
		} catch(IOException ex) {
			System.err.println(ex);
		}

		return exported;
	}

	/**
	 * Exports all artifact information that the user has access to.
	 * @return the number of artifacts exported.
	 */
	protected static int exportArtifacts(RemoteApi api, String token, OutputStream out) {
		int exported = 0;

		try {
			CSVPrinter printer = new CSVPrinter(out);
			printer.writeln(new String[] {
				"id",
				"parent",
				"project",
				"deleted",
				"name",
				"directory",
				"scopeName",
				"description",
				"descriptionFormat",
				"createdAt",
				"lastModifiedAt",
				"lastAccessAt",
				"owner",
				"lockedBy",
				"lastModifiedBy",
				"keywords",
				"category",
				"comment",
				"length",
				"revision",
				"historyEntries",
				"status",
				"notification"});

			ProjectDto projects[] = api.findAllProjects(token);
			for(int i = 0; i < projects.length; i++) {
				ArtifactDto artifacts[] = api.findTopArtifactsByProject(token, projects[i].getId());
				for(int j = 0; j < artifacts.length; j++)
					exported += exportArtifact(api, token, printer, artifacts[j]);
			}
		} catch(IOException ex) {
			System.err.println(ex);
		}

		return exported;
	}

	/**
	 * Recursively exports the given artifact and its children if there are.
	 * @return the number of artifacts exported.
	 */
	protected static int exportArtifact(RemoteApi api, String token, CSVPrinter printer, ArtifactDto artifact) throws IOException {
		int exported = 1;

		printer.writeln(new String[] {
			artifact.getId().toString(),
			(artifact.getParent() != null) ? writeInteger(artifact.getParent().getId()) : null,
			writeProject(artifact.getProject()),
			writeBoolean(Boolean.valueOf(artifact.isDeleted())),
			artifact.getName(),
			writeInteger(artifact.getTypeId()),
			artifact.getScopeName(),
			artifact.getDescription(),
			artifact.getDescriptionFormat(),
			writeDate(artifact.getCreatedAt()),
			writeUser(artifact.getOwner()),
			writeInteger(artifact.getVersion()),
			writeLong(artifact.getFileSize()),
			writeNamed(artifact.getStatus()),
			writeDate(artifact.getLastModifiedAt()),
			writeUser(artifact.getLastModifiedBy()),
			(artifact.getAdditionalInfo() != null) ? writeUser(artifact.getAdditionalInfo().getLockedBy()) : null,
			(artifact.getAdditionalInfo() != null) ? writeInteger(artifact.getAdditionalInfo().getPublishedRevision()) : null,
			(artifact.getAdditionalInfo() != null) ? writeInteger(artifact.getAdditionalInfo().getKeptHistoryEntries()) : null,
			writeInteger(artifact.getNotification()) });

		// traverse children recursively
		if(artifact.isDirectory()) {
			ArtifactDto children[] = api.findArtifactsByParentArtifact(token, artifact.getId());
			for(int i = 0; i < children.length; i++)
				exported += exportArtifact(api, token, printer, children[i]);
		}

		return exported;
	}

	/**
	 * Exports all tracker information that the user has access to.
	 * @return the number of trackers exported.
	 */
	protected static int exportTrackers(RemoteApi api, String token, OutputStream out) {
		int exported = 0;

		try {
			CSVPrinter printer = new CSVPrinter(out);
			printer.writeln(new String[] {
				"id",
				"type",
				"project",
				"createdBy",
				"name",
				"description",
				"descriptionFormat",
				"visible",
				"createdAt" });

			TrackerDto trackers[] = api.findAllTrackers(token);
			for(int i = 0; i < trackers.length; i++) {
				TrackerDto tracker = trackers[i];
				printer.writeln(new String[] {
					writeInteger(tracker.getId()),
					writeNamed(tracker.getType().getId(), tracker.getType().getName()),
					writeProject(tracker.getProject()),
					writeUser(tracker.getCreatedBy()),
					tracker.getName(),
					tracker.getDescription(),
					tracker.getDescriptionFormat(),
					writeBoolean(tracker.getVisible()),
					writeDate(tracker.getCreatedAt()) });

				exported++;
			}
		} catch(IOException ex) {
			System.err.println(ex);
		}

		return exported;
	}

	final private static String[] TrackerHeader = {
			"id",
			"tracker",
			"assignedTo",
			"milestones",
			"versions",
			"supervisors",
			"platforms",
			"OSes",
			"status",
			"categories",
			"priority",
			"submitter",
			"submitterEmail",
			"modifiedAt",
			"assignedAt",
			"submittedAt",
			"closedAt",
			"summary",
			"description",
			"descriptionFormat",
			"startDate",
			"endDate",
			"resolutions",
			"severities",
			"approver",
			"fileRevisionAtSubmit",
			"fileRevisionAtClose",
			"deleted",
			"estimatedHours",
			"spentHours"
			};
	/**
	 * Exports all tracker item information.
	 * @return the number of tracker items exported.
	 */
	protected static int exportTrackerItems(RemoteApi api, String token, OutputStream out) {
		int exported = 0;

		try {
			CSVPrinter printer = new CSVPrinter(out);
			printer.writeln(TrackerHeader);

			TrackerDto trackers[] = api.findAllTrackers(token);
			for(int i = 0; i < trackers.length; i++) {
				TrackerItemDto items[] = api.findTrackerItemsByTrackerId(token, trackers[i].getId());

				for(int j = 0; j < items.length; j++, exported++) {
					TrackerItemDto item = items[j];

					writeTrackerItem(printer, item);
				}
			}
		} catch(IOException ex) {
			System.err.println(ex);
		}

		return exported;
	}

	protected static int exportUserTrackerItems(RemoteApi api, String token, OutputStream out) {
		int exported = 0;

		try {
			CSVPrinter printer = new CSVPrinter(out);
			printer.writeln(TrackerHeader);

			TrackerItemDto items[] = api.findAllUserTrackerItems(token);
			for(int j = 0; j < items.length; j++) {
				TrackerItemDto item = items[j];

				writeTrackerItem(printer, item);

				exported++;
			}
		} catch(IOException ex) {
			System.err.println(ex);
		}

		return exported;
	}

	private static void writeTrackerItem(CSVPrinter printer, TrackerItemDto item) throws IOException {
		printer.writeln(new String[] {
			writeInteger(item.getId()),
			writeTracker(item.getTracker()),
			writeUserList(item.getAssignedTo()),
			writeOptionList(item.getMilestones()),
			writeOptionList(item.getVersions()),
			writeUserList(item.getSupervisors()),
			writeOptionList(item.getPlatforms()),
			writeOptionList(item.getSubjects()),
			writeNamed(item.getStatus()),
			writeOptionList(item.getCategories()),
			writeInteger(item.getPriority()),
			writeUser(item.getSubmitter()),
			writeDate(item.getModifiedAt()),
			writeDate(item.getAssignedAt()),
			writeDate(item.getSubmittedAt()),
			writeDate(item.getClosedAt()),
			item.getName(),
			item.getDescription(),
			item.getDescriptionFormat(),
			writeDate(item.getStartDate()),
			writeDate(item.getEndDate()),
			writeOptionList(item.getResolutions()),
			writeOptionList(item.getSeverities()),
			writeInteger(item.getTemplate() != null ? item.getTemplate().getId() : null),
			writeBoolean(Boolean.valueOf(item.isDeleted())),
			writeLong(item.getEstimatedMillis()),
			writeLong(item.getSpentMillis()) });
	}

	// - helpers --------------------------------------------------------------

	private static String writeInteger(Integer value) {
		return (value != null) ? value.toString() : null;
	}

	private static String writeLong(Long value) {
		return (value != null) ? value.toString() : null;
	}

	private static String writeBoolean(Boolean value) {
		return (value != null) ? value.toString() : null;
	}

	private static String writeDate(Date value) {
		return (value != null) ? DATE_FORMAT.format(value) : null;
	}

	private static String writeUser(UserDto value) {
		return (value != null) ? writeNamed(value.getId(), value.getName()) : null;
	}

	private static String writeProject(ProjectDto value) {
		return (value != null) ? writeNamed(value.getId(), value.getName()) : null;
	}

	private static String writeTracker(TrackerDto value) {
		return (value != null) ? writeNamed(value.getId(), value.getName()) : null;
	}

	private static String writeNamed(IdentifiableDto value) {
		if (value instanceof NamedDto) {
			NamedDto named = (NamedDto) value;
			return writeNamed(named.getId(), named.getName());
		}
		return null;
	}

	/** In the current implementation, there is only a single user returned from the server. */
	private static String writeUserList(List value) {
		if((value == null) || value.isEmpty())
			return null;

		return writeUser((UserDto)value.get(0));
	}

	/** In the current implementation, there is only a single choice returned from the server. */
	private static String writeOptionList(List value) {
		if((value == null) || value.isEmpty())
			return null;

		TrackerChoiceOptionDto option = (TrackerChoiceOptionDto)value.get(0);
		return writeNamed(option.getId(), option.getName());
	}

	private static String writeNamed(Integer id, String name) {
		return (id == null) ? null : id + ";" + name;
	}
}
