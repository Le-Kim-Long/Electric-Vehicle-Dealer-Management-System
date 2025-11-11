package org.example.entity;

import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealerCarId implements Serializable {
    private Integer carId;
    private Integer dealerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealerCarId that = (DealerCarId) o;
        return Objects.equals(carId, that.carId) && Objects.equals(dealerId, that.dealerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(carId, dealerId);
    }
}
