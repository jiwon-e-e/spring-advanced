package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;
    // 생성자에 들어가는 애들도 다 mock 으로 선언해줘야함

    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        weatherClient = new WeatherClient(restTemplateBuilder);
    }

    @Test
    @DisplayName("실패 - API 상태코드가 OK 가 아님 ")
    void getTodayWeather_failed_by_api_statusCode_not_OK() {
        // given
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessageContaining("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: 502");
    }

    @Test
    @DisplayName("실패 - weatherArray 가 null")
    void getTodayWeather_failed_by_weatherArray_is_null() {
        // given
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("날씨 데이터가 없습니다.");
    }

    @Test
    @DisplayName("실패 - 빈 응답배열")
    void getTodayWeather_failed_by_response_is_empty() {
        // given
        WeatherDto[] emptyArray = new WeatherDto[0];
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(emptyArray, HttpStatus.OK);
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("날씨 데이터가 없습니다.");
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 날씨데이터")
    void getTodayWeather_failed_by_date_not_found() {
        // given
        WeatherDto otherDay = new WeatherDto("2023-01-01", "Sunny");
        WeatherDto[] weatherArray = {otherDay};
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(weatherArray, HttpStatus.OK);

        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다.");
    }
}