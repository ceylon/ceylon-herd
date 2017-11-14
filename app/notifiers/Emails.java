/********************************************************************************
 * Copyright (c) {date} Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package notifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import models.Comment;
import models.Module;
import models.ModuleComment;
import models.Project;
import models.User;
import models.UserStatus;
import play.mvc.Mailer;

public class Emails extends Mailer {
	
	private static final String FROM = "Ceylon Herd Module Repository <ceylon-herd@inforealm.org>";
	private static final String SUBJECT_PREFIX = "[Ceylon Herd] ";

	public static void confirm(User user) {
		setSubject(SUBJECT_PREFIX + "Please confirm your email address");
		addRecipient(user.email);
		setFrom(FROM);
		send(user);
	}

    public static void confirmEmailModification(User user, String recipient) {
        setSubject(SUBJECT_PREFIX + "Please confirm your email address");
        addRecipient(recipient);
        setFrom(FROM);
        send(user);
    }
	
    public static void resetPassword(User user) {
        setFrom(FROM);
        setSubject(SUBJECT_PREFIX + "Please reset your password");
        addRecipient(user.email);
        send(user);
    }

	public static void projectClaimNotification(Project project, User user) {
		setSubject(SUBJECT_PREFIX + "New project claim for "+project.moduleName+" from "+user.userName);
		setFrom(FROM);
		for(String recipient : getNotificationRecipients((Project)null, user)){
			setRecipient(recipient);
			send(project, user);
		}
	}

	public static void projectStatusNotification(Project project, User user) {
		String status = project.status.toString().toLowerCase();
		setSubject(SUBJECT_PREFIX + "Your project claim "+project.moduleName+" has been "+status);
		setFrom(FROM);
		for(String recipient : getNotificationRecipients(project, user)){
			setRecipient(recipient);
			send(project, status);
		}
	}
	
    public static void projectEditedNotification(Project project, User user) {
        setSubject(SUBJECT_PREFIX + "Project claim for " + project.moduleName + " has been edited from " + user.userName);
        setFrom(FROM);
        for (String recipient : getNotificationRecipients(project, user)) {
            setRecipient(recipient);
            send(project, user);
        }
    }

    public static void moduleCommentNotification(ModuleComment comment, User user) {
        Module module = comment.module;
        setSubject(SUBJECT_PREFIX + "New comment from "+user.userName+" on module "+module.name);
        setFrom(FROM);
        for(String recipient : getNotificationRecipients(module, user)){
            setRecipient(recipient);
            send(module, comment, user);
        }
    }

	public static void commentNotification(Comment comment, User user) {
		Project project = comment.project;
		setSubject(SUBJECT_PREFIX + "New comment from "+user.userName+" on project claim "+project.moduleName);
		setFrom(FROM);
		for(String recipient : getNotificationRecipients(project, user)){
			setRecipient(recipient);
			send(project, comment, user);
		}
	}

	public static void addAdminNotification(Module module, User admin, User user) {
		setSubject(SUBJECT_PREFIX + "You are now admin on the "+module.name + " module");
		setFrom(FROM);
		addRecipient(admin.email);
		
		send(module, admin, user);
	}

    public static void askForParticipation(Project ownedProject, User requestingUser) {
        setSubject(SUBJECT_PREFIX + requestingUser.userName+" is requesting permission from you to participate on module "+ownedProject.moduleName);
        setFrom(FROM);
        addRecipient(ownedProject.owner.email);
        
        send(ownedProject, requestingUser);
    }

	private static String[] getNotificationRecipients(Project project, User user) {
		// every admin except current user
		Set<User> users = new HashSet<User>(); 
		users.addAll(User.find("isAdmin = ? AND id != ? AND status = ?", true, user.id, UserStatus.REGISTERED).<User>fetch());
		// possibly the project claimer if not current user
		if(project != null && project.owner != user)
			users.add(project.owner);
        return getEmails(users);
	}

	private static String[] getNotificationRecipients(Module module, User user) {
	    // every commenter/owner/admin except current user
	    Set<User> users = new HashSet<User>(); 
	    for(ModuleComment comment : module.comments){
	        users.add(comment.owner);
	    }
	    users.add(module.owner);
        for(User admin : module.admins){
            users.add(admin);
        }
        // not the current user
        users.remove(user);
        return getEmails(users);
	}

	private static String[] getEmails(Set<User> users) {
        String[] emails = new String[users.size()];
        Iterator<User> iterator = users.iterator();
        for(int i=0;i<emails.length;i++)
            emails[i] = iterator.next().email;
        return emails;
    }

    private static void setRecipient(String recipient){
        HashMap<String, Object> map = infos.get();
        List<String> list = new ArrayList<String>();
        list.add(recipient);
        map.put("recipients", list);
	}
}
