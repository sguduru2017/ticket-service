package com.sguduru.ticketservice.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sguduru.ticketservice.exception.SeatHoldException;
import com.sguduru.ticketservice.exception.SeatReserveException;
import com.sguduru.ticketservice.exception.SeatsUnavailableException;
import com.sguduru.ticketservice.model.SeatHoldInput;
import com.sguduru.ticketservice.util.IntIDGenerator;
import com.sguduru.ticketservice.model.SeatHold;

@Service("ticketService")
public class TicketServiceImpl implements TicketService {

	@Value("${venue.capacity}")
	private int venueCapacity;
	
	@Value("${venue.expireinterval}")
	private int holdExpireSeconds;
	
	// Represents the number of seats reserved by the customers
	private int reservedSeats;

	// Hold seats with seatHoldId/customerEmail and returns the seatHold up on successful hold
	private Map<Integer, SeatHoldInput> holdSeatCollection = new ConcurrentHashMap<>();

	// Reserved seat listing with customerEmail along with Set of Integer seating numbers
	private final Map<String, Set<Integer>> reservations = new ConcurrentHashMap<>();
	
	private List<String> venueSeatList;

	public static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

	public TicketServiceImpl() {
	}

	//Constructor to initialize and run  JUnit test using TestService
	public TicketServiceImpl(Integer capacity, int holdExpireSeconds) {
		this.venueCapacity = capacity;
		this.holdExpireSeconds = holdExpireSeconds;
		this.venueSeatList = Arrays.asList(new String[this.venueCapacity]);

	}

	public synchronized int numSeatsAvailable() {

		removeExpired(Instant.now());
		logger.info("Fetching available venue seats");

		// total venue capacity - (hold + reserved)		
		return venueCapacity - (holdSeatCollection.entrySet().stream().mapToInt(e -> e.getValue().getNumSeats()).sum()
				+ reservedSeats);
	}

	public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		
		logger.info("Received Request to hold [" + numSeats + "]  seats for customer: " + customerEmail);
		Duration expireAfter = Duration.ofSeconds(holdExpireSeconds);

		if (numSeats < 1) {
			throw new SeatHoldException("At least one seat must be reserved.");
		}
		
		Instant currentTime = Instant.now();
		logger.info("numSeats requested: [" + numSeats + "] numSeat available: " + numSeatsAvailable(currentTime));
		
		if (numSeats > numSeatsAvailable(currentTime)) {
			logger.error("Seats requested exceed the capacity of remaining seats");
			throw new SeatsUnavailableException("Seats requested exceed the capacity of remaining seats");
		}
		
		SeatHoldInput holdInput = new SeatHoldInput(numSeats, customerEmail, currentTime.plus(expireAfter));
		holdInput.setSeatHoldId(IntIDGenerator.generate());
		holdSeatCollection.put(holdInput.getSeatHoldId(), holdInput);
		return holdInput;
	}

	public synchronized String reserveSeats(int seatHoldId, String customerEmail) {

		int holdSeatCount=0;
		Instant currentTime = Instant.now();
		SeatHoldInput seatHoldInput = holdSeatCollection.get(seatHoldId);

		if (null == seatHoldInput || seatHoldInput.isReservedTimeExpired(currentTime)) {
			logger.error("No Hold exist for the given seatHoldId..or hold time expired");
			throw new SeatHoldException("The holdId is either invalid or expired");
		}
		
		logger.info("Request to reserve holdId [" + seatHoldInput.getSeatHoldId() + "]  for customer: " + seatHoldInput.getCustomerEmail());

		if (!seatHoldInput.getCustomerEmail().equalsIgnoreCase(customerEmail)) {
			logger.error("No Email found for the holdId");
			throw new SeatReserveException("No Email found for the holdId");
		}
		
		String confirmationId = UUID.randomUUID().toString();
		holdSeatCount = seatHoldInput.getNumSeats();
		
		Set<Integer> seatsAssigned = new HashSet<>();
		holdSeatCollection.remove(seatHoldId);

		try {
			ListIterator<String> reservedIterator = venueSeatList.listIterator();

			for (int seatNumber = 0; seatNumber < holdSeatCount; ) {
				if (reservedIterator.next() == null) {
					seatNumber++;
					reservedIterator.set(confirmationId);
					reservedSeats++;
					seatsAssigned.add(reservedSeats);
				}
			}
			
		} catch (Exception e)
		{
			//Failed to reserve seats..put the hold back and wait for the expired timer for removal
			logger.error("Failed to reserve the seats for given holdId / customerEmail");
			holdSeatCollection.put(seatHoldId, seatHoldInput);
		}
		
		reservations.put(confirmationId, seatsAssigned);
		return confirmationId;
	}

	private synchronized int numSeatsAvailable(Instant now) {

		removeExpired(now);
		return venueCapacity - (holdSeatCollection.entrySet().stream().mapToInt(e -> e.getValue().getNumSeats()).sum()
				+ reservedSeats);
	}

	private synchronized void removeExpired(Instant currentTime) {

		logger.debug("Removing expired hold seats");
		for (Map.Entry<Integer, SeatHoldInput> entry : holdSeatCollection.entrySet()) {
			if (entry.getValue().isReservedTimeExpired(currentTime))
				holdSeatCollection.remove(entry.getKey());
		}
	}

	@Autowired
	private void initializeVenue(@Value("${venue.capacity}") int venueCapacity) {
		venueSeatList = Arrays.asList(new String[venueCapacity]);
	}
}
