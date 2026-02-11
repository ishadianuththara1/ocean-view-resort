package com.oceanview.model;

public class Reservation {

    private int    reservationId;
    private String reservationNumber;
    private int    guestId;
    private String guestName;
    private String address;
    private String contactNumber;
    private String email;
    private String roomType;
    private String checkInDate;
    private String checkOutDate;
    private double totalAmount;
    private String status;
    private transient int createdBy;

    public Reservation() {}

    public int    getReservationId()     { return reservationId;     }
    public String getReservationNumber() { return reservationNumber; }
    public int    getGuestId()           { return guestId;           }
    public String getGuestName()         { return guestName;         }
    public String getAddress()           { return address;           }
    public String getContactNumber()     { return contactNumber;     }
    public String getEmail()             { return email;             }
    public String getRoomType()          { return roomType;          }
    public String getCheckInDate()       { return checkInDate;       }
    public String getCheckOutDate()      { return checkOutDate;      }
    public double getTotalAmount()       { return totalAmount;       }
    public String getStatus()            { return status;            }
    public int    getCreatedBy()         { return createdBy;         }

    public void setReservationId(int reservationId)            { this.reservationId     = reservationId;     }
    public void setReservationNumber(String reservationNumber) { this.reservationNumber = reservationNumber; }
    public void setGuestId(int guestId)                        { this.guestId           = guestId;           }
    public void setGuestName(String guestName)                 { this.guestName         = guestName;         }
    public void setAddress(String address)                     { this.address           = address;           }
    public void setContactNumber(String contactNumber)         { this.contactNumber     = contactNumber;     }
    public void setEmail(String email)                         { this.email             = email;             }
    public void setRoomType(String roomType)                   { this.roomType          = roomType;          }
    public void setCheckInDate(String checkInDate)             { this.checkInDate       = checkInDate;       }
    public void setCheckOutDate(String checkOutDate)           { this.checkOutDate      = checkOutDate;      }
    public void setTotalAmount(double totalAmount)             { this.totalAmount       = totalAmount;       }
    public void setStatus(String status)                       { this.status            = status;            }
    public void setCreatedBy(int createdBy)                    { this.createdBy         = createdBy;         }
}
