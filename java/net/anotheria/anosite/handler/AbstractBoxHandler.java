package net.anotheria.anosite.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.anotheria.anosite.content.bean.BoxBean;
import net.anotheria.anosite.gen.asresourcedata.service.ASResourceDataServiceFactory;
import net.anotheria.anosite.gen.asresourcedata.service.IASResourceDataService;
import net.anotheria.anosite.gen.aswebdata.data.Box;
import net.anotheria.asg.exception.ASGRuntimeException;

public abstract class AbstractBoxHandler implements BoxHandler{

	
	private static IASResourceDataService resourceService = ASResourceDataServiceFactory.createASResourceDataService();
	
	public BoxHandlerResponse process(HttpServletRequest req, HttpServletResponse res, Box box, BoxBean bean)  throws ASGRuntimeException{
		return new ResponseContinue();
	}

	public BoxHandlerResponse submit(HttpServletRequest req, HttpServletResponse res, Box box)  throws ASGRuntimeException{
		return new ResponseContinue();
	}
	
	protected static IASResourceDataService getResourceDataService(){
		return resourceService;
	}

}