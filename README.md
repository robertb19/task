DB model:

![DB model](DBModel.png)

How to run the dockerized application:
- docker build -t robb/todos:latest -t robb/todos:0.0.1 .
- docker run -p 8080:8080 robb/todos:0.0.1
- ng serve in the ./frontend/tasks directory

Frontend Notes (missing front-end items, had to skip due to time constraints):
- Customization of the color of the time picker
- Adding the option to add the deadline at a specfiic time using the time picker
- Update population of the deadline field
- Screens for a single Task Category / Task (although that would be semi useful at the moment as we can already see all the information displayed)
- Responsiveness as the site is not really responsive at the current state
- Tests on the UI elements

Items to improve on the front-end
- Much better component reusability
- The whole look of the site

Backend Notes:
- The specific architectural approach, CQRS together with semi-Hexagonal architecture was chosen in order to be more flexible with future
additions of new serialization methods (as we have REST only now) or switch outs/separation to other database - eg. if we wanted to have a different 
DB for reads and writes. The separate model for writes and reads provides us with that flexibility for the future, even though we use one H2 DB now
 (H2 as it was provided in the excercise)
- I decided to call it "Semi-Hexagonal Architecture" as I do allow Spring and Lombok dependencies there, reasoning being
  that while I believe that Hexagonal architecture makes a lot of sense for being quickly able to exchange databases or add different types of serialization or publishing due to ports
  Spring and Lombok is not something that is often exchanged, or needed to open up an additional interface for
- With regards to the provided DB model, I usually prefer not to expose my DB primary keys in order to manipulate data. However since we do not really have business keys
  (maybe only name in TaskCategory could be used as one), I had to go ahead with that approach and problems that arise with it (such as hash code implementation in entities)
- I could throw Hibernate exceptions directly from the JPA rather than catching them, logging and throwing a custom application one. However, then the whole application
becomes dependent on the Hibernate exception wherever I catch it. I prefer to catch expected exceptions and rethrow domain ones, so that everything in the code
relies upon the domain
- I am aware I could update the DB entity using one SQL rather than fetching it and later updating it, and in general execute more queries with JOINs rather than let Hibernate generate two queries (one for tasks, one for category). 
But since premature optimizations are the root of all evil and I believe the method I have done it is simpler and more readable, and we can always in the future migrate to a query that is more performant. At this stage, I do not see performance issues,
and the cost of running two queries instead of one is marginal, while in my view it provides better readability and is easier to upkeep
- In pure CQRS commands should not return anything, however I believe that there is no harm in returning just the identifier like I did in this project
- Dirties context in IT at the moment is not useful as there is not state being kept (everything is stateless). However if we added some cache or anything like that, then it would be very useful
to already have it provided in the IT. Therefore I added it as a precaution only.

As I am aware there is still things missing inside of the app that I would deem as good to have, however with the time being relatively short,
here is a list of improvements I would add:
- Add much more logging (requests and responses on debug level), think about where to log some additional errors or important information
- Change out IT to Cucumber to provide much more readability of the IT, and move them to another project
- Implement a validator for the commands and queries, right now they only act as insight for the developer, however nothing is checking the core of the domain
whether the passed arguments are valid. We are safe as the REST controllers and DB constraints are protecting us and validating us but if we wanted to add another serialization/deserialization mechanism
then we wouldn't be fully sure
- Add more verbosity to the errors in requests. When you pass some parameters I return 400 at times without too much context, I'd work more on being much more user-friendly and add better
verbosity to the errors. However I did try to handle the main and most important scenarios, plus the documentation in OpenAPI is protecting us too
- Exchange exceptions which are not "exceptional" such as NotFoundException, to errors (such as Vavr collection with Either object). Exceptions are not errors and I let the exceptions here steer my code which
is not good practice. With more time I'd exchange the exceptions with error objects (Go-language-way, utilizing Vavr) and let my code steer the app rather than exceptions
- I'd change the null parameters I pass from requests to the further objects to Optionals. I made that decision at the beginning, and now I regret it, Optionals would have been
much better as they directly indicate that the passed value is in fact nullable
- Downgrade H2, as I'm using the latest Flyway but it has not fully been tested with the new H2 version