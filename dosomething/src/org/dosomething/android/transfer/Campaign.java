package org.dosomething.android.transfer;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.dosomething.android.DSConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;


public class Campaign implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy", Locale.US);
	
	private String id;
	private String name;
	private String logoUrl;
	private Date startDate;
	private Date endDate;
	private String teaser;
	private String backgroundColor;
	private String backgroundUrl;
	private String callout;
	private int minVersion;
	private int[] cause_tags;
	private int gid;
	private int order;
	private DSConstants.CAMPAIGN_TYPE campaignType;

	private String videoUrl;
	private String videoThumbnail;
	private String additionalText;
	private String link;
	private String image;
	private String shareTitle;
	private String shareMessage;
	private String signUpAltLink;
	private String signUpAltText;
	private String signUpSmsAction;
	
	private String smsReferText;
	private int mCommonsAlphaOptIn;
	private int mCommonsBetaOptIn;
	
	private List<Faq> faqs;
	private Gallery gallery;
	private List<HowTo> howTos;
	private Prize prize;
	private MoreInfo moreInfo;
	private List<Resource> resources;
	private List<Challenge> challenges;
	private WebForm reportBack;
	private WebForm signUp;
	private SFGData sfgData;

	public Campaign() {}
	
	public Campaign(String id, JSONObject obj) throws JSONException, ParseException {
		
		JSONObject co = obj.getJSONObject("campaign");
		
		this.id = id;
		
		name = co.getString("campaign-name");
		backgroundColor = "#" + co.getString("logo-bg-color");
		startDate = df.parse(co.getString("start-date"));
		endDate = df.parse(co.getString("end-date"));
		logoUrl = co.getString("logo");
		backgroundUrl = co.optString("logo-bg-image",null);
		callout = co.optString("call-to-action",null);
		gid = co.optInt("gid", -1);
		order = co.getInt("order");
		smsReferText = co.optString("sms-refer-text");
		mCommonsAlphaOptIn = co.optInt("mcommons-optin", -1);
		mCommonsBetaOptIn = co.optInt("mcommons-friend-optin", -1);
		minVersion = co.optInt("android-min-version", 0);
		
		JSONArray ct = co.optJSONArray("causes-tags");
		if(ct!=null) {
			int numTags = ct.length();
			if(numTags > 0)
				cause_tags = new int[11];	// 11 for number of causes
			
			for(int i=0; i<numTags; i++) {
				int tag = ct.getInt(i);
				cause_tags[i] = tag;
			}
		}
		
		// Convert campaign-type to its enum value
		String strCampaignType = co.optString("campaign-type",null);
		if (strCampaignType != null) {
			if (strCampaignType.equals("change-a-mind"))
				campaignType = DSConstants.CAMPAIGN_TYPE.CHANGE_A_MIND;
			else if (strCampaignType.equals("donation"))
				campaignType = DSConstants.CAMPAIGN_TYPE.DONATION;
			else if (strCampaignType.equals("help-1-person"))
				campaignType = DSConstants.CAMPAIGN_TYPE.HELP_1_PERSON;
			else if (strCampaignType.equals("improve-a-place"))
				campaignType = DSConstants.CAMPAIGN_TYPE.IMPROVE_A_PLACE;
			else if (strCampaignType.equals("made-by-you"))
				campaignType = DSConstants.CAMPAIGN_TYPE.MADE_BY_YOU;
			else if (strCampaignType.equals("share-for-good"))
				campaignType = DSConstants.CAMPAIGN_TYPE.SHARE_FOR_GOOD;
			else if (strCampaignType.equals("sms"))
				campaignType = DSConstants.CAMPAIGN_TYPE.SMS;
		}
		
		JSONObject m = obj.optJSONObject("main");
		if(m!=null) {
			teaser = m.optString("teaser",null);
			videoUrl = m.optString("video",null);
			videoThumbnail = m.optString("video-thumbnail",null);
			additionalText = m.optString("additional-text",null);
			link = m.optString("link",null);
			image = m.optString("image",null);
			shareTitle = m.optString("share-title",null);
			shareMessage = m.optString("share-message",null);
			signUpAltLink = m.optString("sign-up-alt-link",null);
			signUpAltText = m.optString("sign-up-alt-text",null);
			signUpSmsAction = m.optString("sign-up-sms-action",null);
		}
		
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
		
		JSONObject mi = obj.optJSONObject("more-info");
		if (mi != null) {
			moreInfo = new MoreInfo(mi);
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
		
		JSONObject sfg = obj.optJSONObject("sfg-data");
		if (sfg != null) {
			sfgData = new SFGData(sfg);
		}
	}
	
	public Intent getShareIntent(){
		Intent answer = new Intent(android.content.Intent.ACTION_SEND);
		
		if(!nullOrEmpty(shareTitle)){
			answer.putExtra(android.content.Intent.EXTRA_SUBJECT, shareTitle);
		}
		
		answer.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
		answer.setType("text/plain");
		return answer;
	}
	
	private static boolean nullOrEmpty(String str){
		return str == null || str.trim().length() == 0;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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

	public String getVideoThumbnail() {
		return videoThumbnail;
	}

	public void setVideoThumbnail(String videoThumbnail) {
		this.videoThumbnail = videoThumbnail;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
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
	
	public MoreInfo getMoreInfo() {
		return moreInfo;
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
	
	public SFGData getSFGData() {
		return sfgData;
	}

	public String getShareTitle() {
		return shareTitle;
	}

	public void setShareTitle(String shareTitle) {
		this.shareTitle = shareTitle;
	}

	public String getShareMessage() {
		return shareMessage;
	}
	
	public String getSignUpAltLink() {
		return signUpAltLink;
	}
	
	public String getSignUpAltText() {
		return signUpAltText;
	}
	
	public String getSignUpSmsAction() {
		return signUpSmsAction;
	}

	public void setShareMessage(String shareMessage) {
		this.shareMessage = shareMessage;
	}
	public String getBackgroundUrl() {
		return backgroundUrl;
	}

	public void setBackgroundUrl(String backgroundUrl) {
		this.backgroundUrl = backgroundUrl;
	}
	
	public String getCallout() {
		return callout;
	}
	
	public DSConstants.CAMPAIGN_TYPE getCampaignType() {
		return campaignType;
	}
	
	public int getMinVersion() {
		return minVersion;
	}
	
	public int[] getCauseTags() {
		return cause_tags;
	}
	
	public int getGid() {
		return gid;
	}
	
	public int getOrder() {
		return order;
	}
	
	public String getSMSReferText() {
		return smsReferText;
	}
	
	public int getMCommonsAlphaOptIn() {
		return mCommonsAlphaOptIn;
	}
	
	public int getMCommonsBetaOptIn() {
		return mCommonsBetaOptIn;
	}
}
