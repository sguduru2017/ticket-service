package com.sguduru.ticketservice.model;

import com.sguduru.ticketservice.util.IntIDGenerator;

/* This class will hold the reserved seat details for a given customer email and seatholdId*/

public class SeatHold {

		protected int numSeats;
		protected int seatHoldId;
		protected String customerEmail;
		
		public SeatHold() {

	    }
	    
	    public SeatHold(int numSeats, String customerEmail) {
	        this.numSeats = numSeats;
	        this.customerEmail = customerEmail;
	    }
		
	    @Override
		public String toString() {
			return "SeatHold [numSeats=" + numSeats + ", seatHoldId=" + seatHoldId + ", customerEmail=" + customerEmail + "]";
		}

		public int getNumSeats() {
			return numSeats;
		}

		public void setNumSeats(int numSeats) {
			this.numSeats = numSeats;
		}

		public int getSeatHoldId() {
			return seatHoldId;
		}

		public void setSeatHoldId(int seatHoldId) {
			this.seatHoldId = seatHoldId;
		}

		public String getCustomerEmail() {
			return customerEmail;
		}

		public void setCustomerEmail(String customerEmail) {
			this.customerEmail = customerEmail;
		}
		
}
