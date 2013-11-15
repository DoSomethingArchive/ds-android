package org.dosomething.android.transfer;

import android.content.Intent;

import org.dosomething.android.DSConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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
	private boolean hidden;
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
	private int signUpSmsOptIn;
	
	private String smsReferText;
	private int mCommonsAlphaOptIn;
	private int mCommonsBetaOptIn;
	
	private List<Faq> faqs;
	private Gallery gallery;
	private List<HowTo> howTos;
	private People people;
	private Prize prize;
	private MoreInfo moreInfo;
	private List<Resource> resources;
	private List<Challenge> challenges;
	private WebForm reportBack;
	private WebForm signUp;
	private SFGData sfgData;

    // List of data to display in the Do It campaign section
    private List<ICampaignSectionData> mDoItData;

    // List of data to display in the Learn campaign section
    private List<ICampaignSectionData> mLearnData;

    // List of data to display in the Plan campaign section
    private List<ICampaignSectionData> mPlanData;

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
		hidden = co.optBoolean("hidden", false);
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
			signUpSmsOptIn = m.optInt("sign-up-sms-opt-in", 0);
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
		
		JSONObject jsonPeople = obj.optJSONObject("people");
		if (jsonPeople != null) {
			people = new People(jsonPeople);
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

        JSONArray doItData = obj.optJSONArray("do-it");
        if (doItData != null) {
            mDoItData = convertToSectionData(doItData);
        }

        JSONArray learnData = obj.optJSONArray("learn");
        if (learnData != null) {
            mLearnData = convertToSectionData(learnData);
        }

        JSONArray planData = obj.optJSONArray("plan");
        if (planData != null) {
            mPlanData = convertToSectionData(planData);
        }
	}

    /**
     * Convert JSONArray of section data to an ICampaignSectionData list.
     *
     * @param jsonData JSONArray of section data
     * @return Converted list of ICampaignSectionData objects
     * @throws JSONException
     */
    private List<ICampaignSectionData> convertToSectionData(JSONArray jsonData) throws JSONException {
        ArrayList<ICampaignSectionData> sectionData = new ArrayList<ICampaignSectionData>(jsonData.length());

        for (int i = 0; i < jsonData.length(); i++) {
            JSONObject data = jsonData.getJSONObject(i);
            String type = data.getString("type");
            if (type.equals(CampaignTextImageData.TYPE_ID)) {
                sectionData.add(new CampaignTextImageData(data));
            }
            else if (type.equals(CampaignImageTextData.TYPE_ID)) {
                sectionData.add(new CampaignImageTextData(data));
            }
            else if (type.equals(CampaignGalleryData.TYPE_ID)) {
                sectionData.add(new CampaignGalleryData(data));
            }
        }

        return sectionData;
    }
	
	public JSONObject toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		
		JSONObject jsonCampaign = new JSONObject();
		jsonCampaign.put("campaign-name", name);
		// Remove '#' from color value
		String logoBgColor = backgroundColor.substring(1);
		jsonCampaign.put("logo-bg-color", logoBgColor);
		jsonCampaign.put("start-date", new SimpleDateFormat("MM/dd/yy", Locale.US).format(startDate));
		jsonCampaign.put("end-date", new SimpleDateFormat("MM/dd/yy", Locale.US).format(endDate));
		jsonCampaign.put("logo", logoUrl);
		jsonCampaign.put("logo-bg-image", backgroundUrl);
		jsonCampaign.put("call-to-action", callout);
		jsonCampaign.put("gid", gid);
		jsonCampaign.put("hidden", hidden);
		jsonCampaign.put("order", order);
		jsonCampaign.put("sms-refer-text", smsReferText);
		jsonCampaign.put("mcommons-optin", mCommonsAlphaOptIn);
		jsonCampaign.put("mcommons-friend-optin", mCommonsBetaOptIn);
		jsonCampaign.put("android-min-version", minVersion);
		obj.put("campaign", jsonCampaign);
		
		if (cause_tags != null) {
			JSONArray jsonTags = new JSONArray();
			for (int i = 0; i < cause_tags.length; i++) {
				jsonTags.put(cause_tags[i]);
			}
			obj.put("causes-tags", jsonTags);
		}
		
		if (campaignType != null) {
			String cType = "";
			if (campaignType == DSConstants.CAMPAIGN_TYPE.CHANGE_A_MIND)
				cType = "change-a-mind";
			else if (campaignType == DSConstants.CAMPAIGN_TYPE.DONATION)
				cType = "donation";
			else if (campaignType == DSConstants.CAMPAIGN_TYPE.HELP_1_PERSON)
				cType = "help-1-person";
			else if (campaignType == DSConstants.CAMPAIGN_TYPE.IMPROVE_A_PLACE)
				cType = "improve-a-place";
			else if (campaignType == DSConstants.CAMPAIGN_TYPE.MADE_BY_YOU)
				cType = "made-by-you";
			else if (campaignType == DSConstants.CAMPAIGN_TYPE.SHARE_FOR_GOOD)
				cType = "share-for-good";
			else if (campaignType == DSConstants.CAMPAIGN_TYPE.SMS)
				cType = "sms";
			
			obj.put("campaign-type", cType);
		}
		
		obj.put("campaign", jsonCampaign);
		
		JSONObject main = new JSONObject();
		main.put("teaser", teaser);
		main.put("video", videoUrl);
		main.put("video-thumbnail", videoThumbnail);
		main.put("additional-text", additionalText);
		main.put("link", link);
		main.put("image", image);
		main.put("share-title", shareTitle);
		main.put("share-message", shareMessage);
		main.put("sign-up-alt-link", signUpAltLink);
		main.put("sign-up-alt-text", signUpAltText);
		main.put("sign-up-sms-action", signUpSmsAction);
		main.put("sign-up-sms-opt-in", signUpSmsOptIn);
		obj.put("main", main);
		
		if (faqs != null && faqs.size() > 0) {
			JSONArray jsonFaqs = new JSONArray();
			for (Faq f : faqs) {
				jsonFaqs.put(f.toJSON());
			}
			obj.put("faq", jsonFaqs);
		}
		
		if (gallery != null)
			obj.put("gallery", gallery.toJSON());
		
		if (howTos != null && howTos.size() > 0) {
			JSONArray jsonHowTos = new JSONArray();
			for (HowTo h : howTos) {
				jsonHowTos.put(h.toJSON());
			}
			obj.put("how-to", jsonHowTos);
		}
		
		if (people != null)
			obj.put("people", people.toJSON());
		
		if (prize != null)
			obj.put("prizes", prize.toJSON());

		if (moreInfo != null)
			obj.put("more-info", moreInfo.toJSON());

		if (resources != null && resources.size() > 0) {
			JSONArray jsonResources = new JSONArray();
			for (Resource r : resources) {
				jsonResources.put(r.toJSON());
			}
			obj.put("resources", jsonResources);
		}
		
		if (challenges != null && challenges.size() > 0) {
			JSONArray jsonChallenges = new JSONArray();
			for (Challenge c : challenges) {
				jsonChallenges.put(c.toJSON());
			}
			obj.put("challenges", jsonChallenges);
		}

		if (reportBack != null)
			obj.put("report-back", reportBack.toJSON());

		if (signUp != null)
			obj.put("sign-up", signUp.toJSON());

		if (sfgData != null)
			obj.put("sfg-data", sfgData.toJSON());

        if (mDoItData != null && mDoItData.size() > 0) {
            JSONArray doItData = new JSONArray();
            for (ICampaignSectionData data : mDoItData) {
                doItData.put(data.toJSON());
            }
            obj.put("do-it", doItData);
        }

        if (mLearnData != null && mLearnData.size() > 0) {
            JSONArray learnData = new JSONArray();
            for (ICampaignSectionData data : mLearnData) {
                learnData.put(data.toJSON());
            }
            obj.put("learn", learnData);
        }

        if (mPlanData != null && mPlanData.size() > 0) {
            JSONArray planData = new JSONArray();
            for (ICampaignSectionData data : mPlanData) {
                planData.put(data.toJSON());
            }
            obj.put("plan", planData);
        }
		
		JSONObject namedObj = new JSONObject();
		namedObj.put(id, obj);
		
		return namedObj;
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
	
	public People getPeople() {
		return people;
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
	
	public int getSignUpSmsOptIn() {
		return signUpSmsOptIn;
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
	
	public boolean isHidden() {
		return hidden;
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

    public List<ICampaignSectionData> getDoItData() {
        return mDoItData;
    }

    public List<ICampaignSectionData> getLearnData() {
        return mLearnData;
    }

    public List<ICampaignSectionData> getPlanData() {
        return mPlanData;
    }
}
