# forum
[![serverless](http://public.serverless.com/badges/v3.svg)](http://www.serverless.com)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a5e6365423374eeebaaa964692c31fc0)](https://www.codacy.com/gh/uvsy-aws-backend/forum/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=uvsy-aws-backend/forum&amp;utm_campaign=Badge_Grade)
[![Maintainability](https://api.codeclimate.com/v1/badges/23532c50021561b99426/maintainability)](https://codeclimate.com/github/uvsy-aws-backend/forum/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/23532c50021561b99426/test_coverage)](https://codeclimate.com/github/uvsy-aws-backend/forum/test_coverage)

Service that manages the forum logic for students

- [x] Publications
- [ ] Comments
- [ ] Votes
- [ ] Report


### Environment Requirements

- JDK 8
- NodeJS and npm
- Docker (optional)

&nbsp;
### SetUp

    make init

&nbsp;
### Create domain

    make domain

> This action should be performed once, only when the service is first created, before deploy.
>
> e.g.
>
> `make init`
>
> `make domain`
>
> `make deploy`

&nbsp;
### Build

    make build

&nbsp;
### Deploy

    make deploy
