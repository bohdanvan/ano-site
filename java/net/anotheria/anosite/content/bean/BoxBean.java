package net.anotheria.anosite.content.bean;

import java.util.ArrayList;
import java.util.List;

public class BoxBean {
	private BoxTypeBean type;
	private List<BoxBean> subboxes;
	
	private String content;
	private String name;
	private String id;
	private String parameter1;
	private String parameter2;
	private String parameter3;
	private String parameter4;
	private String parameter5;
	private String parameter6;
	private String parameter7;
	private String parameter8;
	private String parameter9;
	private String parameter10;
	
	private AttributeMap attributes;
	
	public BoxBean(){
		subboxes = new ArrayList<BoxBean>();
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParameter1() {
		return parameter1;
	}
	public void setParameter1(String parameter1) {
		this.parameter1 = parameter1;
	}
	public String getParameter10() {
		return parameter10;
	}
	public void setParameter10(String parameter10) {
		this.parameter10 = parameter10;
	}
	public String getParameter2() {
		return parameter2;
	}
	public void setParameter2(String parameter2) {
		this.parameter2 = parameter2;
	}
	public String getParameter3() {
		return parameter3;
	}
	public void setParameter3(String parameter3) {
		this.parameter3 = parameter3;
	}
	public String getParameter4() {
		return parameter4;
	}
	public void setParameter4(String parameter4) {
		this.parameter4 = parameter4;
	}
	public String getParameter5() {
		return parameter5;
	}
	public void setParameter5(String parameter5) {
		this.parameter5 = parameter5;
	}
	public String getParameter6() {
		return parameter6;
	}
	public void setParameter6(String parameter6) {
		this.parameter6 = parameter6;
	}
	public String getParameter7() {
		return parameter7;
	}
	public void setParameter7(String parameter7) {
		this.parameter7 = parameter7;
	}
	public String getParameter8() {
		return parameter8;
	}
	public void setParameter8(String parameter8) {
		this.parameter8 = parameter8;
	}
	public String getParameter9() {
		return parameter9;
	}
	public void setParameter9(String parameter9) {
		this.parameter9 = parameter9;
	}
	public List<BoxBean> getSubboxes() {
		return subboxes;
	}
	public void setSubboxes(List<BoxBean> subboxes) {
		this.subboxes = subboxes;
	}
	public BoxTypeBean getType() {
		return type;
	}
	public void setType(BoxTypeBean type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getRenderer(){
		return type.getRenderer();
	}
	
	public String getTypeName(){
		return type.getName();
	}

	public AttributeMap getAttributes() {
		return attributes;
	}

	public void setAttributes(AttributeMap attributes) {
		this.attributes = attributes;
	}
}