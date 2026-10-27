package com.mycompany.aas.api;

import com.mycompany.aas.model.Kunstwerk;
import java.util.List;

public interface KunstwerkenRepository {

    List<Kunstwerk> search(String query, int limit, int offset) throws Exception;
}
