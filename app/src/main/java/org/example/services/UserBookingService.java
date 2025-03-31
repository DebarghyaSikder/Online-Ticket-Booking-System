package org.example.services;
import java.io.File;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.entities.Train;
import org.example.entities.Ticket;
import org.example.entities.User;
import org.example.util.UserServiceUtil;

// keep the user globally for everyone so that it can use each and every service
//public class UserBookingService {
//    private User user; // user is global. we always have to make an instances to access the services
//
//    // Creating user so that each user can access these functions whoever are stored in my local db
//    private List<User> userList;  //userList is that list which fetches all users from my localdb and stores them in my list
//
//
//
//    // We need the Jackson library so add it to dependencies in build.gradle. this is for using OBJECT MAPPER
//    private ObjectMapper objectMapper=new ObjectMapper(); // object mapper is to serialize and deserialize things. Serialize means when we store an object in json file. And jab json nikalenge User main daalne ke liye then we have to deserialize it
//    private static final String USERS_PATH="app/src/main/java/org/example/localdb/users.json"; //defined a path
//    //static so that none can change my file. we need this in memory since without this we cannot fetch users
//    public UserBookingService(User user) throws IOException {
//
//        { // When a class ka object banta hain constructor is the first thing that gets called
//            // when user is using booking service we assume that he has logged in
//            this.user = user; // jab user log in hogaya we can pass the user to this constructor and we can store the user at global level. Whenever he books ticket or does some other work he does not have to login again. each fuction has access to the user
//
//            loadUserListFromFile();
//
//        }
//
//        public UserBookingService() throws IOException {
//            loadUserListFromFile();
//        }
//
//        private void loadUserListFromFile() throws IOException{
//            File users = new File(USERS_PATH); // file se read karne par user main plain text aya and in next step we have to map it
//            // users.json file main jo bhi text tha woh yaha agaya. Now we have to map it to User entity ke fields
//            userList = objectMapper.readValue(users, new TypeReference<List<User>>() {});  // runtime pe jo cheez generic hoti hain usko resolve karna hota hain using this TypeReference
//            //Type Reference is a Wrapper jo List ko bol raha hain ki pehle isko resolve karo and then deserialize karna
//
//            // TypeReference is used in Object Mapper ke runtime
//
//        }
//
//
//    public Boolean loginUser()
//    {
//        Optional<User> foundUser=userList.stream().filter(user1 ->   // when the user tries to login, user ka name search kiya in the userList irrespective of Case. This is Optional so that even if we are unable to find User it does not return null ptr
//                user1.getName().equalsIgnoreCase(user.getName()) &&
//                UserServiceUtil.checkPassword(user.getPassword(),user1.getHashedPassword())).findFirst();   // findFirst is for the case when there are more users with the same userId and Password. then return the first user
//                return foundUser.isPresent();
//        }
//
//
//        public Boolean signUp(User user1){
//        try {
//            userList.add(user1);
//            saveUserListToFile();
//            return Boolean.TRUE;
//        }
//            catch(IOException ex){
//                return Boolean.FALSE;
//            }
//        }
//
//
//        private void saveUserListToFile() throws IOExeption{
//
//        }
//
//
//
//}
//}

public class UserBookingService {

    private ObjectMapper objectMapper = new ObjectMapper();
    private List<User> userList;
    private User user;
    private final String USER_FILE_PATH = "org/example/localdb/user.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {  // user has just entered but not logged in. I have loaded all the users from my local db
        File users = new File(USER_FILE_PATH);
        if (users.exists() && users.length() != 0) {
            userList = objectMapper.readValue(users, new TypeReference<List<User>>() {});
        } else {
            userList = new ArrayList<>();
        }
    }

    public Boolean loginUser() {
        return userList.stream().anyMatch(user1 ->
                user1.getName().equals(user.getName()) &&
                        UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword()));
    }

    public Boolean signUp(User user1) {
        try {
            userList.add(user1);  // global db main user daal diya but local db main abi tak store nahi kiya hain. I have to add it in the user.json file
            saveUserListToFile();
            return Boolean.TRUE;
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        objectMapper.writeValue(new File(USER_FILE_PATH), userList);  // serialization occurs here since users File main hum List ko daal rahe hain
    }

    // json--> Object(Here, class User) --> deserialization
    // Object(User) --> json --> Serialization

    public void fetchBookings() {
        userList.stream()
                .filter(user1 -> user1.getName().equals(user.getName()) &&
                        UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword()))
                .findFirst()
                .ifPresentOrElse(User::printTickets, () -> System.out.println("No bookings found for this user."));
    }

    public Boolean cancelBooking(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be null or empty.");
            return Boolean.FALSE;
        }

        boolean removed = user.getTicketsBooked() != null &&
                user.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(ticketId));

        if (removed) {
            System.out.println("Ticket with ID " + ticketId + " has been canceled.");
            return Boolean.TRUE;
        } else {
            System.out.println("No ticket found with ID " + ticketId);
            return Boolean.FALSE;
        }
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return true;
                }
            }
            return false;
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }
}
