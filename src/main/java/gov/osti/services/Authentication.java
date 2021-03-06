package gov.osti.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.osti.entity.User;

@Path("authentication")
public class Authentication {

private static Logger log = LoggerFactory.getLogger(Authentication.class);

	
public Authentication() {
	
}

@GET
@Path ("/check")
public Response check() {

	//Require that the user be admin to access
	if(!UserServices.getCurrentUser().hasRole("Admin")) {
		return ErrorResponse
                .forbidden("Permission denied.")
                .build();
	}
	
    User user = UserServices.getCurrentUser();

    return Response.ok().build();
}
}
