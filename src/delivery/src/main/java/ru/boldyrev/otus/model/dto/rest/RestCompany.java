package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.entity.Company;

@Data
@NoArgsConstructor
public class RestCompany {
    String companyName;
    public RestCompany(Company company){
        this.companyName = company.getCompanyName();
    }
}
