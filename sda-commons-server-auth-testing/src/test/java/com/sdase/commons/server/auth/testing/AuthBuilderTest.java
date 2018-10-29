package com.sdase.commons.server.auth.testing;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthBuilderTest {

   private AuthBuilder authBuilder = AuthRule.builder().build().auth();

   @Test
   public void shouldAddIntegerClaim() {
      String token = authBuilder.addClaim("testKey", 42).buildToken();
      Claim claim = JWT.decode(token).getClaim("testKey");
      assertThat(claim.asInt()).isEqualTo(42);
   }

   @Test
   public void shouldAddLongClaim() {
      String token = authBuilder.addClaim("testKey", 2L + Integer.MAX_VALUE).buildToken();
      Claim claim = JWT.decode(token).getClaim("testKey");
      assertThat(claim.asLong()).isEqualTo(2147483649L);
   }

   @Test
   public void shouldAddStringClaim() {
      String token = authBuilder.addClaim("testKey", "hello").buildToken();
      Claim claim = JWT.decode(token).getClaim("testKey");
      assertThat(claim.asString()).isEqualTo("hello");
   }

   @Test
   public void shouldAddBooleanClaim() {
      String token = authBuilder.addClaim("testKey", true).buildToken();
      Claim claim = JWT.decode(token).getClaim("testKey");
      assertThat(claim.asBoolean()).isEqualTo(true);
   }

   @Test
   public void shouldAddDoubleClaim() {
      String token = authBuilder.addClaim("testKey", 3.141D).buildToken();
      Claim claim = JWT.decode(token).getClaim("testKey");
      assertThat(claim.asDouble()).isEqualTo(3.141D);
   }

   @Test
   public void shouldAddDateClaim() {
      Date testDate = new Date();
      String token = authBuilder.addClaim("testKey", testDate).buildToken();
      Claim claim = JWT.decode(token).getClaim("testKey");
      assertThat(claim.asDate()).isEqualToIgnoringMillis(testDate);
   }

   @Test
   public void shouldAddStringArrayClaim() {
      String token = authBuilder.addClaim("testKey", new String[] {"Hello", "World"}).buildToken();
      Claim claim = JWT.decode(token).getClaim("testKey");
      assertThat(claim.asList(String.class)).containsExactly("Hello", "World");
   }

   @Test
   public void shouldAddLongArrayClaim() {
      String token = authBuilder.addClaim("testKey", new Long[] {1L, 2L}).buildToken();
      Claim claim = JWT.decode(token).getClaim("testKey");
      assertThat(claim.asList(Long.class)).containsExactly(1L, 2L);
   }

   @Test
   public void shouldAddIntArrayClaim() {
      String token = authBuilder.addClaim("testKey", new Integer[] {1, 2}).buildToken();
      Claim claim = JWT.decode(token).getClaim("testKey");
      assertThat(claim.asList(Integer.class)).containsExactly(1, 2);
   }

   @Test
   public void shouldAddAllSupportedTypesWithOneCall() {
      Date dateValue = new Date();
      Map<String, Object> claims = new HashMap<>();
      claims.put("s", "Hello");
      claims.put("i", 42);
      claims.put("l", 2L + Integer.MAX_VALUE);
      claims.put("d", 3.141D);
      claims.put("b", true);
      claims.put("s[]", new String[] {"Hello", "World"});
      claims.put("i[]", new Integer[] {1, 2});
      claims.put("l[]", new Long[] {1L, 2L});
      claims.put("date", dateValue);
      String token = authBuilder.addClaims(claims).buildToken();
      DecodedJWT jwt = JWT.decode(token);
      assertThat(jwt.getClaims().get("s").asString()).isEqualTo("Hello");
      assertThat(jwt.getClaims().get("i").asInt()).isEqualTo(42);
      assertThat(jwt.getClaims().get("l").asLong()).isEqualTo(2147483649L);
      assertThat(jwt.getClaims().get("d").asDouble()).isEqualTo(3.141D);
      assertThat(jwt.getClaims().get("b").asBoolean()).isEqualTo(true);
      assertThat(jwt.getClaims().get("s[]").asList(String.class)).containsExactly("Hello", "World");
      assertThat(jwt.getClaims().get("i[]").asList(Integer.class)).containsExactly(1, 2);
      assertThat(jwt.getClaims().get("l[]").asList(Long.class)).containsExactly(1L, 2L);
      assertThat(jwt.getClaims().get("date").asDate()).isEqualToIgnoringMillis(dateValue);
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldFailForAnyClaimOfInvalidType() {
      Date dateValue = new Date();
      Map<String, Object> claims = new LinkedHashMap<>();
      claims.put("s", "Hello");
      claims.put("i", 42);
      claims.put("l", 2L + Integer.MAX_VALUE);
      claims.put("d", 3.141D);
      claims.put("b", true);
      claims.put("s[]", new String[] {"Hello", "World"});
      claims.put("i[]", new Integer[] {1, 2});
      claims.put("l[]", new Long[] {1L, 2L});
      claims.put("date", dateValue);
      claims.put("invalid", Instant.now());
      authBuilder.addClaims(claims).buildToken();
   }

   @Test
   public void shouldOverwriteClaimOnMultipleAddCalls() {
      String token = authBuilder.addClaim("test", 1L).addClaim("test", 2).addClaim("test", "foo").buildToken();
      DecodedJWT jwt = JWT.decode(token);
      assertThat(jwt.getClaim("test").asLong()).isNull();
      assertThat(jwt.getClaim("test").asInt()).isNull();
      assertThat(jwt.getClaim("test").asString()).isEqualTo("foo");
   }
}