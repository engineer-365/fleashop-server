/*
 * MIT License
 *
 * Copyright (c) 2020 engineer365.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.engineer365.platform.user.test;

import org.engineer365.platform.user.api.UserApiV1;
import org.engineer365.platform.user.api.enums.AccountType;
import org.engineer365.platform.user.api.req.CreateUserByEmailReq;
import org.engineer365.test.IntegrationTestBase;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.engineer365.test.TestContainersFactory;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;


@Testcontainers
@Execution(ExecutionMode.SAME_THREAD) // testcontainers junit5模块不支持parallel
public class UserApiV1IT extends IntegrationTestBase<UserApiV1> {

  public UserApiV1IT() {
    super(UserApiV1.class, UserApiV1.Path.BASE);
  }

  @Rule
  @Container
  final static DockerComposeContainer<?> CONTAINERS = TestContainersFactory.DEFAULT.build();

  @Override
  public DockerComposeContainer<?> containers() {
    return CONTAINERS;
  }

  @BeforeEach
  public void beforeEach() {
    truncateTable("user_user");
    truncateTable("user_account");
  }

  @Test
  public void test_createUserByEmail() throws Exception {
    var api = getApiClient();

    Assertions.assertNull(api.getUser("xxx"));

    var req = CreateUserByEmailReq.builder()
                .name("engineer365-builder")
                .fullName("engineer365 builder")
                .email("engineer365-builder@mail.engineer365.org")
                .password("change me")
                .build();
    var account = api.createUserByEmail(req);
    
    Assertions.assertNotNull(account);
    Assertions.assertEquals(AccountType.EMAIL, account.getType());
    Assertions.assertEquals(req.getEmail(), account.getCredential());

    var user = api.getUser(account.getUserId());
    Assertions.assertEquals(req.getFullName(), user.getFullName());
    Assertions.assertEquals(account.getId(), user.getPrimaryAccountId());

    assertThat(api.getAccount(account.getId()), samePropertyValuesAs(account));
    assertThat(api.getAccountByEmail(req.getEmail()), samePropertyValuesAs(account));
  }

}
