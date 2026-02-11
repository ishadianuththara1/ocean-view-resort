package com.oceanview.model;

public class Guest {

    private int    guestId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String address;
    private String contactNumber;
    private String email;

    public Guest() {}

    public int    getGuestId()        { return guestId;        }
    public String getFirstName()      { return firstName;      }
    public String getLastName()       { return lastName;       }
    public String getFullName()       { return fullName;       }
    public String getAddress()        { return address;        }
    public String getContactNumber()  { return contactNumber;  }
    public String getEmail()          { return email;          }

    public void setGuestId(int guestId)                { this.guestId        = guestId;        }
    public void setFirstName(String firstName)         { this.firstName      = firstName;      }
    public void setLastName(String lastName)           { this.lastName       = lastName;       }
    public void setFullName(String fullName)           { this.fullName       = fullName;       }
    public void setAddress(String address)             { this.address        = address;        }
    public void setContactNumber(String contactNumber) { this.contactNumber  = contactNumber;  }
    public void setEmail(String email)                 { this.email          = email;          }
}
