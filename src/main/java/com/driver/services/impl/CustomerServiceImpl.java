package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteById(customerId);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		Customer customer = customerRepository2.findById(customerId).get();
		List<Driver> driverList = driverRepository2.findAll();
		TripBooking tripBooking;
		try {
//			tripBooking = new TripBooking();
//			tripBooking.setFromLocation(fromLocation);
//			tripBooking.setToLocation(toLocation);
//			tripBooking.setDistanceInKm(distanceInKm);
//			tripBooking.setStatus(TripStatus.CANCELED);
			Driver availableDriver = null;
			for(Driver driver : driverList){
				if(driver.getCab().getAvailable()){
					availableDriver = driver;
					break;
				}
			}
			Cab cab = availableDriver.getCab();
			cab.setAvailable(false);
			availableDriver.setCab(cab);

			tripBooking = new TripBooking(fromLocation,toLocation,distanceInKm,cab.getPerKmRate()*distanceInKm,customer,availableDriver);
			tripBooking.setStatus(TripStatus.CONFIRMED);
			List<TripBooking>bookedTrip =  availableDriver.getTripBookingList();
			bookedTrip.add(tripBooking);
			availableDriver.setTripBookingList(bookedTrip);


			// in customer tripBookingList
			List<TripBooking>bookedTripForCustomer =  customer.getTripBookingList();
			bookedTripForCustomer.add(tripBooking);
			customer.setTripBookingList(bookedTripForCustomer);
			customerRepository2.save(customer);


			driverRepository2.save(availableDriver);

		}catch (Exception e){
			throw new Exception("No value present");
		}

		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);

		 tripBookingRepository2.save(tripBooking);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);


		tripBookingRepository2.save(tripBooking);
	}
}
