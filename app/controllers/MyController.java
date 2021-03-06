/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package controllers;

import java.net.HttpURLConnection;

import org.apache.commons.lang.StringUtils;

import models.User;
import play.Logger;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.results.Error;
import play.mvc.results.Status;

public class MyController extends Controller {
	
    @Before
    static void before(){
        Logger.info("[%s] %s %s", Security.connected(), request.method, request.path);
        // Make sure there's no CSRF for website forms
        if(request.method.equals("POST")
           || request.method.equals("PUT")
           // Don't check that for the REST API which requires user basic auth
           // This should work since web users can't use basic auth in theory
           && StringUtils.isEmpty(request.user)){
            checkAuthenticity();
        }
    }
	
    protected static boolean validationFailed() {
        if(Validation.hasErrors()) {
            prepareForErrorRedirect();
            return true;
        }
        return false;
	}

    protected static void prepareForErrorRedirect() {
		params.flash(); // add http parameters to the flash scope
		Validation.keep(); // keep the errors for the next request
	}

    protected static User getUser() {
		return (User) renderArgs.get("user");
	}
    
	protected static void created() {
		throw new Status(HttpURLConnection.HTTP_CREATED);
	}

	protected static void noContent() {
		throw new Status(HttpURLConnection.HTTP_NO_CONTENT);
	}

    protected static void badRequest(String error) {
        throw new Error(HttpURLConnection.HTTP_BAD_REQUEST, error);
    }
}
