create view chat_message_pair_ai_log as (
  select
    related_message_pair as id,
    string_agg(case when x.type = 'generate-sql' then x.request end, '//') as prompt,
    string_agg(case when x.type = 'generate-sql' then x.response end, '//') as response1,
    string_agg(case when x.type = 'explain-sql' then x.response end, '//') as response2
  from
    (
      select
        col.related_message_pair,
        col.openai_request -> 'messages' -> -1 ->> 'content' as request,
        col.openai_response -> 'choices' -> 0 -> 'message' ->> 'content' as response,
        col.type
      from chat_openai_log col
    ) x
  group by x.related_message_pair
);

create view chat_message_pair_details as (
  select
    cu.id as user_id,
    cmp.id as message_id,
    cmp.conversation,
    cmp.response_timestamp,
    cmp.user_rating,
    cmp.user_comment,
    cmp.openai_total_tokens,
    cmp.response_json ->> 'data' is not null as responded_data,
    cmp.response_json -> 'internal' ->> 'sql' is not null as responded_sql,
    cmp.response_json ->> 'data' as data,
    cmp.response_json -> 'internal' ->> 'sql' as sql,
    cmpal.prompt,
    cmpal.response1,
    cmpal.response2
  from
    chat_message_pair cmp
    join chat_conversation cc on cmp.conversation = cc.id
    join chat_user cu on cc.owner = cu.id
    left join chat_message_pair_ai_log cmpal on cmp.id = cmpal.id
  order by cmp.response_timestamp desc
);
