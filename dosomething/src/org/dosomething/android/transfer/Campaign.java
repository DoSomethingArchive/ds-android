package org.dosomething.android.transfer;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Campaign implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy");
	
	private String name;
	private String logoUrl;
	private Date startDate;
	private Date endDate;
	private String teaser;
	private String backgroundColor;
	private String videoUrl;
	private String additionalText;
	private String additionalLinkUrl;
	private String additionalImageUrl;
	
	private List<Faq> faqs;
	private Gallery gallery;
	private List<HowTo> howTos;
	private Prize prize;
	private List<Resource> resources;
	private List<Challenge> challenges;
	private WebForm reportBack;
	private WebForm signUp;

	public Campaign() {}
	
	public Campaign(JSONObject obj) throws JSONException, ParseException {
		
		JSONObject co = obj.getJSONObject("campaign");
		
		name = co.getString("campaign-name");
		backgroundColor = "#" + co.getString("logo-bg-color");
		startDate = df.parse(co.getString("start-date"));
		endDate = df.parse(co.getString("end-date"));
		logoUrl = co.getString("logo");
		
		JSONObject m = obj.getJSONObject("main");
		teaser = m.getString("teaser");
		videoUrl = m.optString("video");
		additionalText = m.optString("additional-text");
		additionalLinkUrl = m.optString("additional-link");
		additionalImageUrl = m.optString("additional-image");
		
		JSONArray f = obj.optJSONArray("faq");
		if(f!=null) {
			faqs = new ArrayList<Faq>(f.length());
			for(int i=0; i<f.length(); i++) {
				faqs.add(new Faq(f.getJSONObject(i)));
			}
		}
		
		JSONObject g = obj.optJSONObject("gallery");
		if(g!=null) {
			gallery = new Gallery(g);
		}
		
		JSONArray h = obj.optJSONArray("how-to");
		if(h!=null) {
			howTos = new ArrayList<HowTo>(h.length());
			for(int i=0; i<h.length(); i++) {
				howTos.add(new HowTo(h.getJSONObject(i)));
			}
		}
		
		JSONObject p = obj.optJSONObject("prizes");
		if(p!=null) {
			prize = new Prize(p);
		}
		
		JSONArray r = obj.optJSONArray("resources");
		if(r!=null) {
			resources = new ArrayList<Resource>(r.length());
			for(int i=0; i<r.length(); i++) {
				resources.add(new Resource(r.getJSONObject(i)));
			}
		}
		
		JSONArray c = obj.optJSONArray("challenges");
		if(c!=null) {
			challenges = new ArrayList<Challenge>(c.length());
			for(int i=0; i<c.length(); i++) {
				challenges.add(new Challenge(c.getJSONObject(i)));
			}
		}
		
		JSONObject rb = obj.optJSONObject("report-back");
		if(rb!=null) {
			reportBack = new WebForm(rb);
		}
		
		JSONObject su = obj.optJSONObject("sign-up");
		if(su!=null) {
			signUp = new WebForm(su);
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLogoUrl() {
		return logoUrl;
	}
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	public String getTeaser() {
		return teaser;
	}
	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}
	public String getVideoUrl() {
		return videoUrl;
	}
	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}
	public String getAdditionalText() {
		return additionalText;
	}
	public void setAdditionalText(String additionalText) {
		this.additionalText = additionalText;
	}
	public String getAdditionalLinkUrl() {
		return additionalLinkUrl;
	}
	public void setAdditionalLinkUrl(String additionalLinkUrl) {
		this.additionalLinkUrl = additionalLinkUrl;
	}
	public String getAdditionalImageUrl() {
		return additionalImageUrl;
	}
	public void setAdditionalImageUrl(String additionalImageUrl) {
		this.additionalImageUrl = additionalImageUrl;
	}

	public List<Faq> getFaqs() {
		return faqs;
	}

	public void setFaqs(List<Faq> faqs) {
		this.faqs = faqs;
	}

	public Gallery getGallery() {
		return gallery;
	}

	public void setGallery(Gallery gallery) {
		this.gallery = gallery;
	}

	public List<HowTo> getHowTos() {
		return howTos;
	}

	public void setHowTos(List<HowTo> howTos) {
		this.howTos = howTos;
	}

	public Prize getPrize() {
		return prize;
	}

	public void setPrize(Prize prize) {
		this.prize = prize;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}
	
	public List<Challenge> getChallenges() {
		return challenges;
	}

	public void setChallenges(List<Challenge> challenges) {
		this.challenges = challenges;
	}

	public WebForm getReportBack() {
		return reportBack;
	}

	public void setReportBack(WebForm reportBack) {
		this.reportBack = reportBack;
	}

	public WebForm getSignUp() {
		return signUp;
	}

	public void setSignUp(WebForm signUp) {
		this.signUp = signUp;
	}
	
}
