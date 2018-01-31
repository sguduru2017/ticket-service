package com.sguduru.ticketservice.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sguduru.ticketservice.model.SeatHold;
import com.sguduru.ticketservice.service.TicketService;
import com.sguduru.ticketservice.util.CustomErrorType;

@RestController
@RequestMapping("/ticketservice")
@Api(value = "Ticket Service", description = "Service to support hold the seats or reserve (commit) the hold seats", produces = "application/json")
public class TicketServiceController {

	public static final Logger logger = LoggerFactory.getLogger(TicketServiceController.class);

	@Autowired
	@Qualifier(value = "ticketService")
	private TicketService ticketService; // Business Service to hold or reserve seating from Venue
    
	public TicketServiceController(final TicketService ticketService) {
        this.ticketService = ticketService;
    }
    
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String displayMessage() {
		return "Welcome to the Ticket Service";
	}

	/*
	 *  Retrieve total available seats
	 */
	@RequestMapping(value = "/status", produces = "application/json", method = RequestMethod.GET)
	@ApiOperation(value = "find the number of seats available within the venue.", response = Integer.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "No open seats available"), })
	public ResponseEntity<?> numSeatsAvailable() {

		Integer numberOfSeatsAvailable = ticketService.numSeatsAvailable();

		if (numberOfSeatsAvailable <= 0) {
			logger.error("No open seats available");
			return new ResponseEntity<>(new CustomErrorType("No seats available.."), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Integer>(numberOfSeatsAvailable, HttpStatus.OK);
	}

	/*
	 * Return specific seats held for a customer
	 */
	@RequestMapping(value = "/holdseats", produces = "application/json", method = RequestMethod.POST)
	@ApiOperation(value = "hold specific number of seats", response = SeatHold.class)
	public ResponseEntity<?> findAndHoldSeats(@RequestBody SeatHold seatHold) {
		
		seatHold = ticketService.findAndHoldSeats(seatHold.getNumSeats(), seatHold.getCustomerEmail());
		return new ResponseEntity<SeatHold>(seatHold, HttpStatus.OK);
	}

	/*
	 * Commit the seats held for given customer by email
	 */
	@RequestMapping(value = "/reserveseats", produces = "application/json", method = RequestMethod.POST)
	@ApiOperation(value = "reserve the hold seats", response = String.class)
	public ResponseEntity<?> reserveSeats(@RequestBody SeatHold seatHold) {
		

		String confirmationId = ticketService.reserveSeats(seatHold.getSeatHoldId(), seatHold.getCustomerEmail());
		return new ResponseEntity<String>(confirmationId, HttpStatus.OK);
	}

}
