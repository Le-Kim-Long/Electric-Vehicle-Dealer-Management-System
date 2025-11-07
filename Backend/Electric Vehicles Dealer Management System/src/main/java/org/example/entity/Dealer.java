package org.example.entity;

    import jakarta.persistence.*;
    import lombok.*;
    import java.util.List;

    @Entity
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Table(name = "DEALER")
    public class Dealer {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "DealerID")
        private Integer dealerId;

        @Column(name = "DealerName")
        private String dealerName;

        @Column(name = "Address")
        private String address;

        @Column(name = "PhoneNumber")
        private String phone;

        @Column(name = "Email")
        private String email;

        @OneToMany(mappedBy = "dealer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<DealerCar> dealerCars;

        @OneToMany(mappedBy = "dealer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<UserAccount> userAccounts;

        @OneToMany(mappedBy = "dealer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<Promotion> promotions;

        @OneToMany(mappedBy = "dealer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<Orders> orders;

        @OneToMany(mappedBy = "dealer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<CarDistributionRequest> carDistributionRequests;
    }