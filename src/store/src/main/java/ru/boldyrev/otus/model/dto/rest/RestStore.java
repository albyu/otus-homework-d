package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.entity.Store;

import javax.persistence.Id;

@Data
@NoArgsConstructor
public class RestStore {

        @Id
        Long id;

        String name;

        String address;

        public RestStore(Store store){
          this.id = store.getId();
          this.name = store.getName();
          this.address = store.getAddress();
        }

}
