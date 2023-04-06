create table chat_user (
    id serial primary key,
    access_key text unique not null,
    quota_cents integer not null,
    note text
);

create index on chat_user (access_key);

create table chat_conversation (
    id serial primary key,
    owner integer not null
        references chat_user (id)
);

create table chat_message_pair (
    id serial primary key,
    conversation integer not null
        references chat_conversation (id),
    response_timestamp timestamp not null,
    user_prompt text not null,
    response_json json not null,
    openai_total_tokens integer not null
);

create table chat_openai_log (
    id serial primary key,
    related_message_pair integer not null
        references chat_message_pair (id),
    openai_request json not null,
    openai_response json not null
);
