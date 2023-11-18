package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.config.SpringSecurityConfig;
import guru.springframework.spring6restmvc.model.BeerDto;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerController.class)
@Import(SpringSecurityConfig.class)
class BeerDtoControllerTest {

    public static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor JWT = jwt().jwt(jwt -> {
        jwt.claims(claims -> {
                    claims.put("scope", "message.read");
                    claims.put("scope", "message.write");
                })
                .subject("messaging-client")
                .notBefore(Instant.now().minusSeconds(5l));
    });
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerService beerService;

    BeerServiceImpl beerServiceImpl;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<BeerDto> beerArgumentCaptor;


    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
    }

    @Test
    void listBeers() throws Exception {
        given(beerService.listBeers(any(), any(), any(), any(), any())).willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25));


        mockMvc.perform(get(BeerController.BEER_PATH)
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", is(3)));
    }

    @Test
    void getBeerByIdNotFound() throws Exception {

        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(BeerController.BEER_PATH + "/" + UUID.randomUUID())
                        .with(JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBeerById() throws Exception {

        BeerDto testBeer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

        given(beerService.getBeerById(testBeer.getId())).willReturn(Optional.of(testBeer));

        mockMvc.perform(get(BeerController.BEER_PATH + "/" + testBeer.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())))
                .andExpect(jsonPath("$.beerStyle", is(testBeer.getBeerStyle().toString())))
                .andExpect(jsonPath("$.upc", is(testBeer.getUpc())));
    }

    @Test
    void saveBeer() throws Exception {

        BeerDto beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

        beer.setVersion(null);
        beer.setId(null);

        given(beerService.saveBeer(any(BeerDto.class))).willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(1));

        mockMvc.perform(post(BeerController.BEER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(JWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void saveBeerNullName() throws Exception {

        BeerDto beer = BeerDto.builder().build();

        given(beerService.saveBeer(any(BeerDto.class))).willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(1));
        mockMvc.perform(post(BeerController.BEER_PATH)
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6)))
                .andReturn();
    }

    @Test
    void updateBeerById() throws Exception {
        BeerDto beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beer));

        mockMvc.perform(put(BeerController.BEER_PATH + "/" + beer.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());

        verify(beerService).updateBeerById(any(UUID.class), any(BeerDto.class));
    }

    @Test
    void updateBeerByIdNullName() throws Exception {
        BeerDto beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

        beer.setBeerName("");
        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beer));

        mockMvc.perform(put(BeerController.BEER_PATH + "/" + beer.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void deleteBeerById() throws Exception {
        BeerDto beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

        given(beerService.deleteBeerById(any())).willReturn(true);

        mockMvc.perform(delete(BeerController.BEER_PATH + "/" + beer.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(beerService).deleteBeerById(uuidArgumentCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void updateBeerPatchId() throws Exception {
        BeerDto beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

        Map<String, Object> beerMap = new HashMap<>();

        beerMap.put("beerName", "new Name");
        given(beerService.patchBeerById(any(), any())).willReturn(Optional.of(beer));


        mockMvc.perform(patch(BeerController.BEER_PATH + "/" + beer.getId())
                        .with(JWT)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(beerMap.get("beerName")).isEqualTo(beerArgumentCaptor.getValue().getBeerName());
    }
}
