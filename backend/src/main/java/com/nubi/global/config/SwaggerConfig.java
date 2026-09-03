package com.nubi.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Nubi API 문서(OpenAPI 3.1 / Swagger UI) 전역 설정.
 *
 * <p>이 프로젝트는 Spring Security 필터체인을 쓰지 않고
 * {@code JwtAuthenticationFilter} 가 Authorization 헤더를 읽어
 * request attribute 에 userId 를 심어주는 방식이다.
 * 따라서 "어떤 엔드포인트가 토큰이 필요한지"를 스프링이 알고 있지 않으므로,
 * 아래 {@link #PUBLIC_ENDPOINTS} 목록을 기준으로 문서에 자물쇠(security)와
 * 401 응답을 자동으로 채워 넣는다.
 *
 * <p>구성 요소
 * <ul>
 *   <li>{@link #nubiOpenAPI()} : 문서 제목/설명/서버/태그/공통 스키마 정의</li>
 *   <li>{@link #controllerTagCustomizer()} : 컨트롤러 클래스명 → 한글 태그로 치환</li>
 *   <li>{@link #securityAndErrorCustomizer()} : 인증이 필요한 API 에 bearerAuth + 공통 에러 응답 부착</li>
 *   <li>{@code GroupedOpenApi} 3종 : 전체 / 사용자 / 관리자 그룹 분리</li>
 * </ul>
 *
 * <p>접속 주소
 * <ul>
 *   <li>Swagger UI : http://localhost:8080/swagger-ui.html</li>
 *   <li>OpenAPI JSON : http://localhost:8080/v3/api-docs</li>
 * </ul>
 */
@Configuration
public class SwaggerConfig {

    /** Components 에 등록되는 보안 스킴 키. Authorize 버튼과 operation 의 security 가 이 이름으로 연결된다. */
    private static final String BEARER_SCHEME = "bearerAuth";

    /** 공통 에러 응답 스키마 이름 ($ref 로 재사용). */
    private static final String ERROR_SCHEMA = "ErrorResponse";

    // ------------------------------------------------------------------
    // 태그 정의 : 컨트롤러 SimpleName -> 문서에 보여줄 이름
    // 앞의 숫자는 Swagger UI 의 태그 정렬(알파벳순)을 고정하기 위한 것이다.
    // 컨트롤러를 추가하면 이 맵과 아래 tags() 에 한 줄씩만 추가하면 된다.
    // ------------------------------------------------------------------
    private static final Map<String, String> CONTROLLER_TAGS = new LinkedHashMap<>();

    static {
        CONTROLLER_TAGS.put("AccountController", "01. 계정 (Account)");
        CONTROLLER_TAGS.put("RoomsController", "02. 숙소 조회 (Rooms)");
        CONTROLLER_TAGS.put("BookingsController", "03. 예약 (Bookings)");
        CONTROLLER_TAGS.put("BookmarksController", "04. 북마크 (Bookmarks)");
        CONTROLLER_TAGS.put("MypageController", "05. 마이페이지 (Mypage)");
        CONTROLLER_TAGS.put("AdminRoomsController", "06. 관리자 - 숙소/사진 (Admin Rooms)");
        CONTROLLER_TAGS.put("AdminBookingsController", "07. 관리자 - 예약 (Admin Bookings)");
    }

    // ------------------------------------------------------------------
    // 토큰 없이 호출 가능한 엔드포인트 (그 외에는 전부 Bearer 토큰 필요)
    // 형식 : "METHOD 경로" / 경로변수는 이름과 무관하게 "{}" 로 정규화해서 비교한다.
    // ------------------------------------------------------------------
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "POST /api/accounts/signup",
            "POST /api/accounts/login",
            "POST /api/accounts/find-id",
            "POST /api/accounts/find-password",
            "PATCH /api/accounts/change-password",
            "GET /api/rooms",
            "GET /api/rooms/{}",
            "GET /api/rooms/{}/reviews"
    );

    // ==================================================================
    // 1. 문서 전체 메타 정보
    // ==================================================================
    @Bean
    public OpenAPI nubiOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버"),
                        new Server().url("/").description("현재 접속한 호스트")
                ))
                .tags(tags())
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, bearerScheme())
                        .addSchemas(ERROR_SCHEMA, errorSchema()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Nubi 숙소 예약 API")
                .version("v1.0.0")
                .description("""
                        게스트하우스 예약 서비스 **Nubi** 의 REST API 명세서입니다.

                        ---
                        ### 인증 방법
                        1. `POST /api/accounts/login` 을 호출하면 **응답 본문이 JWT 토큰 문자열 그 자체**입니다. (JSON 객체가 아님)
                        2. 우측 상단 **Authorize** 버튼을 눌러 토큰만 붙여넣습니다. (`Bearer ` 접두사는 자동으로 붙습니다)
                        3. 이후 자물쇠가 표시된 API 는 `Authorization: Bearer {토큰}` 헤더가 자동 전송됩니다.

                        토큰 유효기간은 `jwt.expiration-ms` 설정값(기본 1시간)입니다.

                        ### 공통 에러 코드
                        에러 응답 본문은 `{ "errorCode": "..." }` 형태입니다.

                        | HTTP | errorCode | 의미 |
                        |------|-----------|------|
                        | 401 | `NEED_SIGNUP` | 토큰이 없거나 만료/변조됨 |
                        | 409 | `BOOKING_LOCK_CONFLICT` | 동일 숙소·날짜에 예약이 동시에 몰려 잠금 충돌 |
                        | 413 | `FILE_TOO_LARGE` | 업로드 파일이 10MB(요청 전체 50MB)를 초과 |

                        ### 데이터 형식
                        - 날짜+시간 : `2026-11-10T15:00:00` (ISO-8601, 타임존 없음)
                        - 시간 : `15:00:00`
                        - 숙소 사진 URL 은 `/uploads/**` 로 정적 서빙됩니다. (예: `http://localhost:8080/uploads/rooms/1/xxx.png`)

                        ### 소유권 규칙
                        관리자 API(`/api/admin/**`)는 **본인이 등록한 숙소**에 대해서만 조회·수정·삭제가 가능하며,
                        타인의 리소스에 접근하면 403 이 반환됩니다.
                        `GET /api/accounts/{userId}` 도 본인 계정만 조회할 수 있습니다.
                        """)
                .contact(new Contact().name("Nubi Backend Team"));
        // 필요하면 .email("...") / .license(new License().name("MIT")) 추가
    }

    /** 문서 상단에 노출될 태그 목록(설명 포함). 선언 순서 + 숫자 접두사로 정렬을 고정한다. */
    private List<Tag> tags() {
        return List.of(
                tag("01. 계정 (Account)", "회원가입, 로그인, 아이디/비밀번호 찾기, 내 정보 조회 및 탈퇴"),
                tag("02. 숙소 조회 (Rooms)", "누구나 호출 가능한 공개 숙소 목록/상세 조회"),
                tag("03. 예약 (Bookings)", "숙소 예약 생성·조회·취소 및 리뷰 작성"),
                tag("04. 북마크 (Bookmarks)", "관심 숙소 찜하기 / 해제"),
                tag("05. 마이페이지 (Mypage)", "내 예약·북마크 요약 정보"),
                tag("06. 관리자 - 숙소/사진 (Admin Rooms)", "호스트가 소유한 숙소 등록·수정·삭제 및 사진 업로드"),
                tag("07. 관리자 - 예약 (Admin Bookings)", "호스트가 소유한 숙소에 들어온 예약 관리")
        );
    }

    private Tag tag(String name, String description) {
        return new Tag().name(name).description(description);
    }

    /** Authorize 버튼에 연결되는 JWT Bearer 스킴. */
    private SecurityScheme bearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("로그인 응답으로 받은 JWT 토큰 문자열만 입력하세요. (`Bearer ` 는 자동 추가)");
    }

    /** {"errorCode": "..."} 공통 에러 바디. */
    private Schema<?> errorSchema() {
        return new ObjectSchema()
                .description("공통 에러 응답")
                .addProperty("errorCode", new StringSchema()
                        .description("에러 식별 코드")
                        .example("NEED_SIGNUP"));
    }

    // ==================================================================
    // 2. 컨트롤러 클래스명으로 자동 생성되는 태그(account-controller 등)를
    //    위에서 정의한 한글 태그로 치환한다.
    //    → 컨트롤러에 @Tag 를 직접 달면 그 값이 그대로 유지된다.
    // ==================================================================
    @Bean
    public GlobalOperationCustomizer controllerTagCustomizer() {
        return (operation, handlerMethod) -> {
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String friendlyTag = CONTROLLER_TAGS.get(controllerName);
            if (friendlyTag == null) {
                return operation;
            }

            List<String> tags = new ArrayList<>(
                    operation.getTags() == null ? List.of() : operation.getTags());
            tags.remove(defaultTagName(controllerName)); // springdoc 이 붙인 kebab-case 태그 제거
            tags.remove(friendlyTag);
            tags.add(0, friendlyTag);
            operation.setTags(tags);
            return operation;
        };
    }

    /** "AdminRoomsController" -> "admin-rooms-controller" (springdoc 의 기본 태그 명명 규칙) */
    private static String defaultTagName(String simpleName) {
        return simpleName.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }

    // ==================================================================
    // 3. 인증이 필요한 API 에 자물쇠 + 공통 에러 응답을 자동 부착한다.
    //    GlobalOpenApiCustomizer 를 쓰면 기본 문서와 아래 그룹 문서에 모두 적용된다.
    //    (OpenApiCustomizer 는 그룹에 적용되지 않으므로 주의)
    // ==================================================================
    @Bean
    public GlobalOpenApiCustomizer securityAndErrorCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((rawPath, pathItem) -> {
                String path = normalizePath(rawPath);
                pathItem.readOperationsMap().forEach((httpMethod, operation) ->
                        decorate(operation, httpMethod, path));
            });
        };
    }

    private void decorate(Operation operation, PathItem.HttpMethod httpMethod, String path) {
        String key = httpMethod.name() + " " + path;

        if (PUBLIC_ENDPOINTS.contains(key)) {
            operation.setSecurity(List.of()); // 자물쇠 없음을 명시
            return;
        }

        operation.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
        putResponse(operation, "401", "인증 실패 - 토큰이 없거나 만료/변조되었습니다.", "NEED_SIGNUP");

        if (path.startsWith("/api/admin/") || path.startsWith("/api/accounts/{}")) {
            putResponse(operation, "403", "권한 없음 - 본인 소유 리소스가 아닙니다.", null);
        }
        if ("POST /api/bookings".equals(key)) {
            putResponse(operation, "409", "예약 잠금 충돌 - 잠시 후 다시 시도하세요.", "BOOKING_LOCK_CONFLICT");
        }
        if (path.endsWith("/images") && httpMethod == PathItem.HttpMethod.POST) {
            putResponse(operation, "413", "업로드 용량 초과 - 파일당 10MB, 요청당 50MB 제한.", "FILE_TOO_LARGE");
        }
    }

    /** 컨트롤러에서 이미 @ApiResponse 로 선언한 코드는 덮어쓰지 않는다. */
    private void putResponse(Operation operation, String code, String description, String errorCode) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }
        if (responses.containsKey(code)) {
            return;
        }

        MediaType mediaType = new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/" + ERROR_SCHEMA));
        if (errorCode != null) {
            mediaType.example(Map.of("errorCode", errorCode));
        }

        responses.addApiResponse(code, new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", mediaType)));
    }

    /** "/api/rooms/{roomId}" -> "/api/rooms/{}" : 경로변수 이름에 의존하지 않도록 정규화 */
    private static String normalizePath(String path) {
        return path.replaceAll("\\{[^/}]+}", "{}");
    }

    // ==================================================================
    // 4. 문서 그룹 (Swagger UI 우측 상단 드롭다운)
    //    group 값은 URL(/v3/api-docs/{group})에 들어가므로 영문으로,
    //    화면에 보이는 이름은 displayName 으로 지정한다.
    // ==================================================================
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("00-all")
                .displayName("00. 전체 API")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("01-user")
                .displayName("01. 사용자 API")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("02-admin")
                .displayName("02. 관리자 API")
                .pathsToMatch("/api/admin/**")
                .build();
    }
}
