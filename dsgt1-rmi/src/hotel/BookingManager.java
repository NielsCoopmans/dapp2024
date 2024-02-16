package hotel;

import java.awt.print.Book;
import java.time.LocalDate;
import java.util.*;

public class BookingManager {

	private Room[] rooms;

	public BookingManager() {
		this.rooms = initializeRooms();
	}

	public Set<Integer> getAllRooms() {
		Set<Integer> allRooms = new HashSet<Integer>();
		Iterable<Room> roomIterator = Arrays.asList(rooms);
		for (Room room : roomIterator) {
			allRooms.add(room.getRoomNumber());
		}
		return allRooms;
	}

	public boolean isRoomAvailable(Integer roomNumber, LocalDate date) {
		Room room = getRoom(roomNumber);
		if(room == null)
			return false;
		for (BookingDetail bookingDetail : room.getBookings()) {
			if (bookingDetail.getDate().equals(date))
				return false;
		}
		return true;
	}
	public Room getRoom(Integer roomNumber){
		for (Room room : rooms) {
			if (room.getRoomNumber().equals(roomNumber))
				return room;
		}
		return null;
	}

	public class RoomNotAvailable extends Exception {

		// Constructor that takes a custom message
		public RoomNotAvailable(String message) {
			super(message);
		}

		// You can also add additional methods or custom behavior if needed
	}

	public void addBooking(BookingDetail bookingDetail) {
		Integer roomNumber = bookingDetail.getRoomNumber();
		LocalDate date = bookingDetail.getDate();
		if(isRoomAvailable(roomNumber,date)) {
			Room room = getRoom(roomNumber);
			List<BookingDetail> booking = room.getBookings();
			booking.add(bookingDetail);
			room.setBookings(booking);
		}
		//throw new RoomNotAvailable("Sorry this room is not available");
	}

	public Set<Integer> getAvailableRooms(LocalDate date) {
		Set<Integer> availableRooms = new HashSet<>();
		for (Room room : rooms) {
			if (isRoomAvailable(room.getRoomNumber(), date)) {
				availableRooms.add(room.getRoomNumber());
			}
		}
		return availableRooms;
	}

	private static Room[] initializeRooms() {
		Room[] rooms = new Room[4];
		rooms[0] = new Room(101);
		rooms[1] = new Room(102);
		rooms[2] = new Room(201);
		rooms[3] = new Room(203);
		return rooms;
	}
}
