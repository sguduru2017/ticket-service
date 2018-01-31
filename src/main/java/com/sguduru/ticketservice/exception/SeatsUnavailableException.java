package com.sguduru.ticketservice.exception;

public class SeatsUnavailableException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SeatsUnavailableException(String message) {
        super(message);
    }

}