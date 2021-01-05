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
package org.engineer365.platform.user.api;

import org.engineer365.platform.user.api.bean.Account;
import org.engineer365.platform.user.api.bean.User;
import org.engineer365.platform.user.api.req.AccountAuthReq;
import org.engineer365.platform.user.api.req.CreateAccountByEmailReq;
import org.engineer365.platform.user.api.req.CreateUserByEmailReq;

import feign.Param;
import feign.RequestLine;

/**
 *
 */
public interface UserApiV1 {

    public static interface Path {
        public static String BASE = "platform/user/api/v1/rest";

        public static String createUserByEmail = "/user/createUserByEmail";
        public static String createAccountByEmail = "/account/createAccountByEmail";
        public static String authByAccount = "/account/authByAccount";
        public static String getUser = "/user/_/{userId}";
        public static String getAccount = "/account/_/{accountId}";
        public static String getAccountByEmail = "/account/getAccountByEmail";
    }

    @RequestLine("POST " + Path.BASE + Path.createUserByEmail)
    Account createUserByEmail(CreateUserByEmailReq req);

    @RequestLine("POST "+ Path.BASE + Path.createAccountByEmail)
    Account createAccountByEmail(CreateAccountByEmailReq req);

    @RequestLine("POST "+ Path.BASE + Path.authByAccount)
    String authByAccount(AccountAuthReq areq);

    @RequestLine("GET "+ Path.BASE + Path.getUser)
    User getUser(@Param String userId);

    @RequestLine("GET "+ Path.BASE + Path.getAccount)
    Account getAccount(@Param String accountId);

    @RequestLine("GET "+ Path.BASE + Path.getAccountByEmail)
    Account getAccountByEmail(@Param String email);


}
