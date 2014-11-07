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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.Ostermiller.util.CSVParser;
import com.intland.codebeamer.manager.AccessRightsException;
import com.intland.codebeamer.manager.util.ArtifactNameConflictException;
import com.intland.codebeamer.manager.util.ChangeVetoedException;
import com.intland.codebeamer.persistence.dto.ArtifactAdditionalInfoDto;
import com.intland.codebeamer.persistence.dto.ArtifactDto;
import com.intland.codebeamer.persistence.dto.ArtifactStatusDto;
import com.intland.codebeamer.persistence.dto.ProjectDto;
import com.intland.codebeamer.persistence.dto.TrackerChoiceOptionDto;
import com.intland.codebeamer.persistence.dto.TrackerDto;
import com.intland.codebeamer.persistence.dto.TrackerItemDto;
import com.intland.codebeamer.persistence.dto.TrackerLayoutLabelDto;
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
 *   <li>importing data from CSV files (in the format written by <code>com.intland.codebeamer.remoting.test.CodeBeamerCsvExporter</code>)</li>
 *   <li>writing data to CodeBeamer</li>
 * </ul>
 * You can use this as skeleton to develop your own
 * importers.
 *
 * @see com.intland.codebeamer.remoting.sample.CodeBeamerCsvExporter
 *
 * @author <a href="mailto:aron.gombas@intland.com">Aron Gombas</a>
 * @version $Id: KlausMehling 2009-10-29 17:28 +0100 23443:3b63eb1c80e4  $
 */
public class CodeBeamerCsvImporter {
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	/**
	 * Main entry point.
	 * Commandline parsing is not elaborated, for real use
	 * please consider using the Apache Commons CLI package.
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 7) {
			System.err.println("Usage: <program> service-URL (for example http://localhost:8080/cb/remote-api)\n"
				+ "login password projects.csv-path "
				+ "artifacts.csv-path trackers.csv-path trackeritems.csv-path [user.csv-path]");
			System.exit(-1);
		}
		String serviceUrl = args[0];
		String login = args[1];
		String password = args[2];
		String projectCsvPath = args[3];
		String artifactCsvPath = args[4];
		String trackerCsvPath = args[5];
		String trackerItemCsvPath = args[6];
		String userCsvPath = args.length > 7 ? args[7] : null;

		System.out.println("Connecting to CodeBeamer web service at " + serviceUrl + "...");
		RemoteApi api = RemoteApiFactory.getInstance().connect(serviceUrl);
		if(api == null) {
			System.err.println("Couldn't connect, is the service URL correct?");
			System.exit(-1);
		}

		System.out.println("Signing in...");
		String token = api.login(login, password);
		ServerInfo serverInfo = api.getServerInfo();
		System.out.println("Signed in to CodeBeamer " + serverInfo.getMajorVersion() + serverInfo.getMinorVersion()
			+ " (" + serverInfo.getBuildDate() + ") running on " + serverInfo.getOs() + "/Java " + serverInfo.getJavaVersion());

		if (userCsvPath != null) {
			System.out.print("Importing users (IDs ignored)...");
			System.out.println(" " + importUsers(api, token, new FileInputStream(userCsvPath)));
		}

		System.out.print("Importing projects (IDs ignored)...");
		System.out.println(" " + importProjects(api, token, new FileInputStream(projectCsvPath)));

		System.out.print("Importing artifacts (IDs ignored)...");
		System.out.println(" " + importArtifacts(api, token, new FileInputStream(artifactCsvPath)));

		System.out.print("Importing trackers (IDs ignored)...");
		System.out.println(" " + importTrackers(api, token, new FileInputStream(trackerCsvPath)));

		System.out.print("Importing tracker items (IDs ignored)...");
		System.out.println(" " + importTrackerItems(api, token, new FileInputStream(trackerItemCsvPath)));

		System.out.println("Signing out...");
		api.logout(token);

		System.out.println("Done");
	}

	/**
	 * Imports all user account information.
	 * @return the number of user accounts imported.
	 */
	protected static int importUsers(RemoteApi api, String token, InputStream in) throws ChangeVetoedException {
		CSVParser parser = new CSVParser(in, ',');
		String values[][] = null;
		try {
			values = parser.getAllValues();
		} catch(IOException ex) {
			System.err.println(ex);
			return 0;
		}

		// parse by skipping the first row
		int imported = 0;
		for(int i = 1; i < values.length; i++) {
			UserDto user = new UserDto();
			// skip ID: that will be auto-generated by CodeBeamer
			user.setName(values[i][1]);
			user.setStatus(values[i][2]);
			user.setHostName(values[i][3]);
			user.setFirstName(values[i][4]);
			user.setLastName(values[i][5]);
			user.setTitle(values[i][6]);
			user.setAddress(values[i][7]);
			user.setZip(values[i][8]);
			user.setCity(values[i][9]);
			user.setState(values[i][10]);
			user.setSourceOfInterest(values[i][11]);
			user.setScc(values[i][12]);
			user.setTeamSize(values[i][13]);
			user.setDivisionSize(values[i][14]);
			user.setCompany(values[i][15]);
			user.setCountry(values[i][16]);
			user.setEmail(values[i][17]);
			user.setEmailClient(values[i][18]);
			user.setPhone(values[i][19]);
			user.setMobile(values[i][20]);
			user.setDateFormatPattern(values[i][21]);
			user.setDateTimeFormatPattern(values[i][22]);
			user.setTimeZonePattern(values[i][23]);
			int downloadLimit = -1;
			try {
				downloadLimit = Integer.parseInt(values[i][24]);
				user.setDownloadLimit(downloadLimit);
				user.setBrowser(values[i][25]);
				user.setSkills(values[i][26]);
			} catch (Exception ex) {
			}
			// skip registry date: that will be specified by CodeBeamer
			// skip last login: that will be specified by CodeBeamer

			if(api.createUser(token, user, user.getPassword()) != null) {
				imported++;
			}
		}

		return imported;
	}

	/**
	 * Imports all project information.
	 * @return the number of projects imported.
	 */
	protected static int importProjects(RemoteApi api, String token, InputStream in) throws AccessRightsException, ChangeVetoedException {
		CSVParser parser = new CSVParser(in, ',');
		String values[][] = null;
		try {
			values = parser.getAllValues();
		} catch(IOException ex) {
			System.err.println(ex);
			return 0;
		}

		// parse by skipping the first row
		int imported = 0;
		for(int i = 1; i < values.length; i++) {
			ProjectDto project = new ProjectDto();
			// skip ID: that will be auto-generated by CodeBeamer
			project.setName(values[i][1]);
			project.setDescription(values[i][2]);
			project.setDescriptionFormat(values[i][3]);
			project.setPropagation(values[i][4]);
			project.setDefaultMemberRoleId(readInteger(values[i][5]));
			project.setAllowedHost(values[i][6]);
			project.setUserName(values[i][7]);
			project.setPassword(values[i][8]);
			project.setStartDate(readDate(values[i][9]));
			project.setEndDate(readDate(values[i][10]));
			// skip createdAt: that will be specified by CodeBeamer
			// skip createdBy: that will be specified by CodeBeamer
			project.setCreatedFromHost(values[i][13]);
			project.setVirtualHost(values[i][14]);
			project.setEnvironment(values[i][15]);
			project.setCategory(values[i][16]);
			try {
				project.setCopyright(values[i][17]);
				project.setNatureLanguage(values[i][18]);
				project.setDevelopmentLanguage(values[i][19]);
				project.setStatus(values[i][20]);
			} catch (Exception ex) {
			}

			if(api.createProject(token, project) != null) {
				imported++;
			}
		}

		return imported;
	}

	/**
	 * Imports all artifact information.
	 * @return the number of artifacts imported.
	 */
	protected static int importArtifacts(RemoteApi api, String token, InputStream in) throws AccessRightsException, ArtifactNameConflictException, ChangeVetoedException {
		CSVParser parser = new CSVParser(in, ',');
		String values[][] = null;
		try {
			values = parser.getAllValues();
		} catch(IOException ex) {
			System.err.println(ex);
			return 0;
		}

		// parse by skipping the first row
		int imported = 0;
		for(int i = 1; i < values.length; i++) {
			ArtifactDto parent = null;
			if(!"".equals(values[i][1])) {
				parent = new ArtifactDto();
				parent.setId(new Integer(values[i][1]));
			}

			ProjectDto project = new ProjectDto();
			project.setId(readId(values[i][2]));

			UserDto owner = new UserDto();
			owner.setId(readId(values[i][10]));

			UserDto lockedBy = new UserDto();
			lockedBy.setId(readId(values[i][16]));

			ArtifactStatusDto status = new ArtifactStatusDto();
			status.setId(readId(values[i][13]));

			ArtifactAdditionalInfoDto additionalInfo = new ArtifactAdditionalInfoDto();
			additionalInfo.setLockedBy(lockedBy);
			additionalInfo.setPublishedRevision(readInteger(values[i][17]));
			additionalInfo.setKeptHistoryEntries(readInteger(values[i][18]));

			ArtifactDto artifact = new ArtifactDto();
			// skip ID: that will be auto-generated by CodeBeamer
			artifact.setParent(parent);
			artifact.setProject(project);
			// skip deleted: that will be specified by CodeBeamer
			artifact.setName(values[i][4]);
			artifact.setTypeId(readInteger(values[i][5]));
			//artifact.setScopeName(values[i][6]);
			artifact.setDescription(values[i][7]);
			artifact.setDescriptionFormat(values[i][8]);
			// skip createdAt: that will be specified by CodeBeamer
			// skip lastModifiedAt: that will be specified by CodeBeamer
			// skip lastAccessAt: that will be specified by CodeBeamer
			artifact.setOwner(owner);
			// skip lockedByUser: that will be specified by CodeBeamer
			// skip lastModifiedByUser: that will be specified by CodeBeamer
			artifact.setStatus(status);
			artifact.setFileSize(readLong(values[i][13]));
			artifact.setAdditionalInfo(additionalInfo);
			artifact.setNotification(readInteger(values[i][19]));

			if(api.createArtifact(token, artifact) != null)
				imported++;
		}

		return imported;
	}

	/**
	 * Imports all tracker information.
	 * @return the number of trackers imported.
	 */
	protected static int importTrackers(RemoteApi api, String token, InputStream in) throws IllegalArgumentException, AccessRightsException, ChangeVetoedException {
		CSVParser parser = new CSVParser(in, ',');
		String values[][] = null;
		try {
			values = parser.getAllValues();
		} catch(IOException ex) {
			System.err.println(ex);
			return 0;
		}

		// parse by skipping the first row
		int imported = 0;
		for(int i = 1; i < values.length; i++) {
			TrackerDto tracker = new TrackerDto();
			tracker.setIssueTypeId(readId(values[i][1]));

			ProjectDto project = new ProjectDto();
			project.setId(readId(values[i][2]));
			tracker.setProject(project);

			UserDto createdBy = new UserDto();
			createdBy.setId(readId(values[i][3]));
			tracker.setCreatedBy(createdBy);

			// skip ID: that will be auto-generated by CodeBeamer
			tracker.setName(values[i][4]);
			tracker.setDescription(values[i][5]);
			tracker.setDescriptionFormat(values[i][6]);
			tracker.setVisible(Boolean.valueOf(values[i][7]));
			// skip created at: that will be specified by CodeBeamer

			if (api.createTracker(token, tracker) != null) {
				imported++;
			}
		}

		return imported;
	}

	/**
	 * Imports all tracker item information.
	 * @return the number of tracker items imported.
	 */
	protected static int importTrackerItems(RemoteApi api, String token, InputStream in) throws Exception {
		CSVParser parser = new CSVParser(in, ',');
		String values[][] = null;
		try {
			values = parser.getAllValues();
		} catch(IOException ex) {
			System.err.println(ex);
			return 0;
		}

		// parse by skipping the first row
		int imported = 0;
		for(int i = 1; i < values.length; i++) {
			TrackerDto tracker = new TrackerDto();
			tracker.setId(readId(values[i][1]));

			UserDto assignee = new UserDto();
			assignee.setId(readId(values[i][2]));

			UserDto supervisor = new UserDto();
			supervisor.setId(readId(values[i][5]));

			UserDto submitter = new UserDto();
			submitter.setId(readId(values[i][11]));

			// download tracker options
			// (it is somewhat redundant to do this for each item, but items might belong to different trackers
			// and this is purely for demonstration purposes, not for high performance)
			TrackerChoiceOptionDto milestoneOptions[] = api.findTrackerChoiceOptions(token, tracker.getId(), new Integer(TrackerLayoutLabelDto.MILESTONES_LABEL_ID));
			TrackerChoiceOptionDto versionOptions[] = api.findTrackerChoiceOptions(token, tracker.getId(), new Integer(TrackerLayoutLabelDto.VERSION_LABEL_ID));
			TrackerChoiceOptionDto platformOptions[] = api.findTrackerChoiceOptions(token, tracker.getId(), new Integer(TrackerLayoutLabelDto.PLATFORM_LABEL_ID));
			TrackerChoiceOptionDto subjectOptions[] = api.findTrackerChoiceOptions(token, tracker.getId(), new Integer(TrackerLayoutLabelDto.SUBJECT_LABEL_ID));
			TrackerChoiceOptionDto categoryOptions[] = api.findTrackerChoiceOptions(token, tracker.getId(), new Integer(TrackerLayoutLabelDto.CATEGORY_LABEL_ID));
			TrackerChoiceOptionDto resolutionOptions[] = api.findTrackerChoiceOptions(token, tracker.getId(), new Integer(TrackerLayoutLabelDto.RESOLUTION_LABEL_ID));
			TrackerChoiceOptionDto severityOptions[] = api.findTrackerChoiceOptions(token, tracker.getId(), new Integer(TrackerLayoutLabelDto.SEVERITY_LABEL_ID));

			TrackerChoiceOptionDto milestone = findOptionById("milestoneOptions", milestoneOptions, readId(values[i][3]));
			TrackerChoiceOptionDto version = findOptionById("versionOptions", versionOptions, readId(values[i][4]));
			TrackerChoiceOptionDto platform = findOptionById("platformOptions", platformOptions, readId(values[i][6]));
			TrackerChoiceOptionDto subject = findOptionById("subjectOptions", subjectOptions, readId(values[i][7]));
			TrackerChoiceOptionDto category = findOptionById("categoryOptions", categoryOptions, readId(values[i][9]));
			TrackerChoiceOptionDto resolution = findOptionById("resolutionOptions", resolutionOptions, readId(values[i][22]));
			TrackerChoiceOptionDto severity = findOptionById("severityOptions", severityOptions, readId(values[i][23]));

			TrackerItemDto item = new TrackerItemDto();
			// skip ID: that will be auto-generated by CodeBeamer
			item.setTracker(tracker);
			if(assignee.getId() != null)
				item.setAssignedTo(asSingleItemList(assignee));
			if(milestone != null)
				item.setMilestones(asSingleItemList(milestone));
			if(version != null)
				item.setVersions(asSingleItemList(version));
			if(supervisor.getId() != null)
				item.setSupervisors(asSingleItemList(supervisor));
			if(platform != null)
				item.setPlatforms(asSingleItemList(platform));
			if(subject != null)
				item.setSubjects(asSingleItemList(subject));
			Integer statusId = readId(values[i][8]);
			if(statusId != null) {
				item.setStatus(new NamedDto(statusId));
			}
			if(category != null)
				item.setCategories(asSingleItemList(category));
			item.setPriority(readInteger(values[i][10]));
			if(submitter.getId() != null)
				item.setSubmitter(submitter);
			item.setModifiedAt(readDate(values[i][13]));
			item.setAssignedAt(readDate(values[i][14]));
			item.setSubmittedAt(readDate(values[i][15]));
			item.setClosedAt(readDate(values[i][16]));
			item.setName(values[i][17]);
			item.setDescription(values[i][18]);
			item.setDescriptionFormat(values[i][19]);
			item.setStartDate(readDate(values[i][20]));
			item.setEndDate(readDate(values[i][21]));
			if(resolution != null)
				item.setResolutions(asSingleItemList(resolution));
			if(severity != null)
				item.setSeverities(asSingleItemList(severity));

			Integer templateId = readId(values[i][24]);
			if (templateId != null) {
				item.setTemplate(new IdentifiableDto(templateId));
			}
			// skip deleted: FALSE as default
			item.setEstimatedMillis(readLong(values[i][28]));
			item.setSpentMillis(readLong(values[i][29]));

			if(api.createTrackerItem(token, item) != null)
				imported++;
		}

		return imported;
	}

	// - helpers --------------------------------------------------------------

	/**
	 * Returns the ID from the given string that has the format <code>ID;name</code> or
	 * <code>null</code> if failed to parse.
	 */
	private static Integer readId(String value) {
		int separator = value.indexOf(';');
		if(separator == -1)
			return null;

		try {
			return new Integer(value.substring(0, separator));
		} catch(NumberFormatException ex) {
			return null;
		}
	}

	/**
	 * Returns the <code>Boolean</code> object parsed from the given string or
	 * <code>null</code> if failed to parse.
	 */
//	private static Boolean readBoolean(String value) {
//		try {
//			return new Boolean(value);
//		} catch(NumberFormatException ex) {
//			return null;
//		}
//	}

	/**
	 * Returns the <code>Integer</code> object parsed from the given string or
	 * <code>null</code> if failed to parse.
	 */
	private static Integer readInteger(String value) {
		try {
			return new Integer(value);
		} catch(NumberFormatException ex) {
			return null;
		}
	}

	/**
	 * Returns the <code>Long</code> object parsed from the given string or
	 * <code>null</code> if failed to parse.
	 */
	private static Long readLong(String value) {
		try {
			return new Long(value);
		} catch(NumberFormatException ex) {
			return null;
		}
	}

	/**
	 * Returns the <code>Date</code> object parsed from the given string or
	 * <code>null</code> if failed to parse.
	 */
	private static Date readDate(String value) {
		try {
			return DATE_FORMAT.parse(value);
		} catch (ParseException ex) {
			return null;
		}
	}

	/** Returns a single-item list containing the given object only. */
	private static List asSingleItemList(Object obj) {
		List list = new ArrayList(1);
		list.add(obj);

		return list;
	}

	/** Returns the options from the given list by ID or <code>null</code> if not found. */
	private static TrackerChoiceOptionDto findOptionById(String name, TrackerChoiceOptionDto options[], Integer id) {
		if(id == null)
			return null;

		for(int i = 0; i < options.length; i++)
			if(options[i].getId().equals(id))
				return options[i];

		System.err.println("Option \"" + id + "\" not found in \"" + name + "\"");
		return null;
	}
}
