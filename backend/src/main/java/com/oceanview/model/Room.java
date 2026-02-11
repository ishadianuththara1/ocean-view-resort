package com.oceanview.model;

public class Room {

    private int     roomId;
    private String  roomNumber;
    private String  roomType;
    private double  pricePerNight;
    private boolean isAvailable;

    public Room() {}

    public int     getRoomId()        { return roomId;        }
    public String  getRoomNumber()    { return roomNumber;    }
    public String  getRoomType()      { return roomType;      }
    public double  getPricePerNight() { return pricePerNight; }
    public boolean isAvailable()      { return isAvailable;   }

    public void setRoomId(int roomId)              { this.roomId        = roomId;        }
    public void setRoomNumber(String roomNumber)   { this.roomNumber    = roomNumber;    }
    public void setRoomType(String roomType)       { this.roomType      = roomType;      }
    public void setPricePerNight(double price)     { this.pricePerNight = price;         }
    public void setAvailable(boolean isAvailable)  { this.isAvailable   = isAvailable;   }


}
