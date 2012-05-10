package org.dosomething.android.cache;

import java.util.Date;
import java.util.List;

import org.dosomething.android.transfer.Campaign;

public class Cache {
	
	private List<Campaign> campaigns;
	private Date lastRetrieved;
	
	public List<Campaign> getCampaigns(){
		List<Campaign> answer = null;
		if(lastRetrieved != null){
			long age = new Date().getTime() - lastRetrieved.getTime();
			
			//if retrieved less than 30 minutes ago
			if(age < 1800000){
				answer = campaigns;
			}
		}
		
		return answer;
	}
	
	public void setCampaigns(List<Campaign> campaigns){
		this.lastRetrieved = new Date();
		this.campaigns = campaigns;
	}
	
}
