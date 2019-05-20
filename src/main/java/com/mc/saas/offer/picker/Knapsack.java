package com.mc.saas.offer.picker;

import java.io.Serializable;

public class Knapsack implements Comparable,Serializable {
	
	/**
	 * @author suqiang.song
	 *
	 */
	private static final long serialVersionUID = 2089093833620573352L;

	public Knapsack(int userId, int userGroupId, int offerId, String merchant, int weight, int value) {
		super();
		this.userId = userId;
		this.userGroupId = userGroupId;
		this.offerId = offerId;
		this.merchant = merchant;
		this.weight = weight;
		this.value = value;
	}
	private int userId;
    private int userGroupId;
    private int offerId;
    private String merchant;
    private int weight;         
    private int value;         
     
    public int getWeight() {      
        return weight;      
    }      
          
    public int getValue() {      
        return value;      
    }
    
          
    
	public int compareTo(Object o) {
		int comparevalue=((Knapsack) o).getValue();
        /* For Decending order*/
        return comparevalue -this.value;
	}      
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int getUserGroupId() {
		return userGroupId;
	}
	public void setUserGroupId(int userGrupId) {
		this.userGroupId = userGrupId;
	}
	public int getOfferId() {
		return offerId;
	}
	public void setOfferId(int offerId) {
		this.offerId = offerId;
	}
	public String getMerchant() {
		return merchant;
	}
	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return userId + ","+ userGroupId+"," + offerId+"," +merchant+","+weight+","+value;
	}
	
	public static String[] getColumns() {
		String[] columns = new String[] { "userId", "userGroupId", "offerId", "weight", "value"};
		return columns;
	}
}
