package com.sguduru.ticketservice.model;

import java.time.Instant;

import com.sguduru.ticketservice.util.IntIDGenerator;

/* This class will hold the reservation details for a given customer email */

public class SeatHoldInput extends SeatHold {

		private Instant reserveUntil;
		
		public SeatHoldInput() {

	    }
	    
	    public SeatHoldInput(int numSeats, String customerEmail, Instant expiredTime) {
	        super(numSeats, customerEmail);
	        this.reserveUntil = expiredTime;
	    }
		
	    @Override
		public String toString() {
			return "SeatHoldInput [reserveUntil=" + reserveUntil + ", getNumSeats()=" + getNumSeats()
					+ ", getSeatHoldId()=" + getSeatHoldId() + ", getCustomerEmail()=" + getCustomerEmail()
					+ ", hashCode()=" + hashCode() + "]";
		}

		public boolean isReservedTimeExpired(Instant expiredTime) {
	        return !reserveUntil.isAfter(expiredTime);
	    }

		public Instant getReserveUntil() {
			return reserveUntil;
		}
	    
		public void setReserveUntil(Instant reserveUntil) {
			this.reserveUntil = reserveUntil;
		}
		
}
