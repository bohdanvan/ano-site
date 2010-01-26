package net.anotheria.anosite.content.filter;

import net.anotheria.anoprise.metafactory.MetaFactory;
import net.anotheria.anoprise.metafactory.MetaFactoryException;
import net.anotheria.anosite.gen.assitedata.data.EntryPoint;
import net.anotheria.anosite.gen.assitedata.data.Site;
import net.anotheria.anosite.gen.assitedata.service.IASSiteDataService;
import net.anotheria.anosite.gen.aswebdata.data.Pagex;
import net.anotheria.anosite.gen.aswebdata.service.IASWebDataService;
import net.anotheria.anosite.gen.shared.service.AnoDocConfigurator;
import net.anotheria.asg.exception.ASGRuntimeException;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
/**
 * With the entry point filter the site is able to define different pages for different subsite or domains. 
 * @author lrosenberg
 *
 */
public class EntryPointFilter implements Filter{

	private static Logger log = Logger.getLogger(EntryPointFilter.class);

	private IASWebDataService webDataService;
	/**
	 * SiteDataService for entrypoints.
	 */
	private IASSiteDataService siteDataService;

	@Override public void destroy() {
		
	}

	@Override public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain) throws IOException, ServletException {
		if (!(sreq instanceof HttpServletRequest))
			return;
		
		HttpServletRequest req = (HttpServletRequest)sreq;
		req.setCharacterEncoding(AnoDocConfigurator.getEncoding());

		String domain = req.getServerName();
		//System.out.println("Request on domain: "+domain);
		//System.out.println(req.getContextPath()+" "+req.getServletPath()+" "+req.getPathInfo()+"?"+req.getQueryString());
		
		EntryPoint forDomain = null;
		EntryPoint defaultEntry = null;
		
		try{
			List<EntryPoint> entryPoints = siteDataService.getEntryPoints();
			for (EntryPoint p : entryPoints){
				if (defaultEntry == null && p.getDefaultEntry()){
					defaultEntry = p;
					//System.out.println("\tFound default Entry: "+defaultEntry);
				}
				List<String> domains = p.getDomains();
				for (String d : domains){
					//System.out.println("checking domain "+d);
					if (d.equals(domain)){
						//System.out.println("found matching domain: "+d+" in entrypoint: "+p);
						forDomain = p;
						break;
					}
				}
				if (forDomain!=null)
					break;
			}
			
			EntryPoint myEntryPoint = forDomain == null ? defaultEntry : forDomain;
			//System.out.println("Will use entry point "+myEntryPoint);
			Pagex targetPage = null;
			if (myEntryPoint==null){
				//ok, no entry point defined, lets find first site and its homepage.
				List<Site> sites = siteDataService.getSites();
				if (sites == null || sites.size()==0){
					log.error("No sites found, dont know where to send user.");
				}else{
					Site firstSite = sites.get(0);
					if (firstSite.getStartpage()==null || firstSite.getStartpage().length()==0){
						log.error("First site doesnt have a startpage.");
					}else{
						targetPage = webDataService.getPagex(firstSite.getStartpage());
					}
				}
			}else{
				if (myEntryPoint.getStartPage()!=null && myEntryPoint.getStartPage().length()>0){
					targetPage = webDataService.getPagex(myEntryPoint.getStartPage());
				}else{
					targetPage = webDataService.getPagex(siteDataService.getSite(myEntryPoint.getStartSite()).getStartpage());
				}
					
			}
			
			
			if (targetPage==null){
				chain.doFilter(sreq, sres);
				return;
			}
			
			String urlQuery = req.getQueryString();
			urlQuery = urlQuery != null && urlQuery.length() > 0? "?" + urlQuery:"";
			String redirect = req.getContextPath()+req.getServletPath()+targetPage.getName()+".html" + urlQuery;
			log.info("Redirecting to: "+redirect);
			((HttpServletResponse)sres).sendRedirect(redirect);
		}catch(ASGRuntimeException e){
			throw new ServletException(e);
		}
		
	}

	@Override public void init(FilterConfig config) throws ServletException {
		try {
			webDataService = MetaFactory.get(IASWebDataService.class);
			siteDataService = MetaFactory.get(IASSiteDataService.class);
		} catch (MetaFactoryException e) {
			log.fatal("ASG services init failure",e);
			throw new ServletException("ASG services init failure",e);
		}
	}

}
