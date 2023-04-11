create table chat_usage_statistics (
    id serial primary key,
    timestamp timestamp not null,
    number_messages integer not null,
    number_openai_tokens integer not null
);
