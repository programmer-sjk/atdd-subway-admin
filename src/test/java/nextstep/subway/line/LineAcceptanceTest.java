package nextstep.subway.line;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.DatabaseCleanup;
import nextstep.subway.station.StationAcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LineAcceptanceTest {
    @LocalServerPort
    int port;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    @BeforeEach
    public void setUp() {
        if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
            RestAssured.port = port;
            databaseCleanup.afterPropertiesSet();
        }
        databaseCleanup.execute();
    }

    /**
     * When 지하철 노선을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
     */
    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // when
        String expectLine = "3호선";
        Long upStationId = StationAcceptanceTest.지하철_역_생성("연신내역")
                .jsonPath().getLong("id");
        Long downStationId = StationAcceptanceTest.지하철_역_생성("불광역")
                .jsonPath().getLong("id");

        ExtractableResponse<Response> response =
                지하철_노선_생성(expectLine, "주황색", upStationId, downStationId, 10);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        List<String> lineNames = 지하철_노선_전체_조회()
                .jsonPath().getList("name", String.class);
        assertThat(lineNames).containsAnyOf(expectLine);
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void showLines() {
        // given
        String expectLine1 = "3호선";
        String expectLine2 = "분당선";

        Long upStationId = StationAcceptanceTest.지하철_역_생성("연신내역")
                .jsonPath().getLong("id");
        Long downStationId = StationAcceptanceTest.지하철_역_생성("불광역")
                .jsonPath().getLong("id");

        지하철_노선_생성(expectLine1, "주황색", upStationId, downStationId, 10);
        지하철_노선_생성(expectLine2, "노랑색", upStationId, downStationId, 10);

        // when
        List<String> lineNames = 지하철_노선_전체_조회()
                .jsonPath().getList("name", String.class);

        // then
        assertThat(lineNames).contains(expectLine1, expectLine2);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void showLine() {
        // given
        String expectLine = "3호선";
        Long upStationId = StationAcceptanceTest.지하철_역_생성("연신내역")
                .jsonPath().getLong("id");
        Long downStationId = StationAcceptanceTest.지하철_역_생성("불광역")
                .jsonPath().getLong("id");

        Long lineId = 지하철_노선_생성(expectLine, "주황색", upStationId, downStationId, 10)
                .jsonPath().getLong("id");

        // when
        String result = 지하철_노선_조회(lineId)
                .jsonPath().getString("name");

        // then
        assertThat(result).isEqualTo(expectLine);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 노선에 포함된 역 정보를 응답받을 수 있다.
     */
    @DisplayName("지하철 노선을 조회하면, 노선에 포함된 지하철 역도 조회된다.")
    @Test
    void showStations() {
        // given
        String stationName1 = "연신내역";
        String stationName2 = "불광역";

        Long upStationId = StationAcceptanceTest.지하철_역_생성("연신내역")
                .jsonPath().getLong("id");
        Long downStationId = StationAcceptanceTest.지하철_역_생성("불광역")
                .jsonPath().getLong("id");

        Long lineId = 지하철_노선_생성("3호선", "주황색", upStationId, downStationId, 10)
                .jsonPath().getLong("id");

        // when
        List<String> stationNames = 지하철_노선_조회(lineId)
                .jsonPath().getList("stations.name", String.class);

        // then
        assertThat(stationNames).contains(stationName1, stationName2);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다.
     */
    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        String expectLineName = "2호선";
        Long upStationId = StationAcceptanceTest.지하철_역_생성("연신내역")
                .jsonPath().getLong("id");
        Long downStationId = StationAcceptanceTest.지하철_역_생성("불광역")
                .jsonPath().getLong("id");

        Long lineId = 지하철_노선_생성("3호선", "주황색", upStationId, downStationId, 10)
                .jsonPath().getLong("id");

        // when
        String result = 지하철_노선_수정(lineId, expectLineName, "주황색")
                .jsonPath().getString("name");

        // then
        assertThat(result).isEqualTo(expectLineName);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 삭제하면
     * Then 해당 지하철 노선 정보는 삭제된다.
     */
    @DisplayName("지하철 노선을 삭제한다.")
    @Test
    void deleteLine() {
        // given
        Long upStationId = StationAcceptanceTest.지하철_역_생성("연신내역")
                .jsonPath().getLong("id");
        Long downStationId = StationAcceptanceTest.지하철_역_생성("불광역")
                .jsonPath().getLong("id");
        Long lineId = 지하철_노선_생성("3호선", "주황색", upStationId, downStationId, 10)
                .jsonPath().getLong("id");

        // when
        지하철_노선_삭제(lineId);

        // then
        List<String> lineIds = 지하철_노선_전체_조회()
                .jsonPath().getList("id", String.class);
        assertThat(lineIds).isEmpty();
    }

    private ExtractableResponse<Response> 지하철_노선_생성(
            String name,
            String color,
            Long upStationId,
            Long downStationId,
            int distance
    ) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("color", color);
        params.put("upStationId", upStationId.toString());
        params.put("downStationId", downStationId.toString());
        params.put("distance", String.valueOf(distance));

        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철_노선_수정(Long id, String name, String color) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("color", color);

        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().patch("/lines/" + id)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철_노선_삭제(Long id) {
        return RestAssured.given().log().all()
                .when().delete("/lines/" + id)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철_노선_전체_조회() {
        return RestAssured.given().log().all()
                .when().get("/lines")
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철_노선_조회(Long id) {
        return RestAssured.given().log().all()
                .when().get("/lines/" + id)
                .then().log().all()
                .extract();
    }
}
