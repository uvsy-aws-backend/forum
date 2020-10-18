# forum
[![serverless](http://public.serverless.com/badges/v3.svg)](http://www.serverless.com)

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
