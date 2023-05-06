-- :name list-t_vote :? :* :D
select id, vote_title, end_time,
case when end_time >= now() then 1 when end_time < now() then 0 else 1 end as status,
status as pub_status from t_vote
/*~
where 1=1 [and vote_title like :l:vote_title] [and status = :status]
[and create_time >= :beginTime] [and create_time <= :endTime]
~*/
order by id
--~ [limit :limit offset :offset]


-- :name list-t_vote-count :? :1 :D
select count(1) as cnt from t_vote
/*~
where 1=1 [and vote_title like :l:vote_title] [and status = :status]
[and create_time >= :beginTime] [and create_time <= :endTime]
~*/

-- :name list-t_vote_question :? :*
select * from t_vote_question where vote_id = :vote_id

-- :name list-t_vote_options :? :*
select * from t_vote_options where question_id = :question_id order by option_order

-- :name list-t_vote_user :? :*
select vu.id, vu.user_id, u.nick_name, u.sex, u.number, d.dept_name, p.post_name from t_vote_user vu
left join sys_user u on vu.user_id = u.user_id
left join sys_dept d on u.dept_id = d.dept_id
left join sys_user_post up on u.user_id = up.user_id
left join sys_post p on up.post_id = p.post_id
where vu.vote_id = :vote_id

-- :name list-my_todo_vote :? :* :D
select v.* from t_vote v left join t_vote_user vu on vu.vote_id = v.id
where vu.user_id = :user_id and status = 1 and (end_time > now() or end_time is null)
  and v.id not in (select vote_id from t_vote_answer where user_id = :user_id)
order by v.id
--~ [limit :limit offset :offset]

-- :name list-my_todo_vote-count :? :1
select count(1) as cnt from t_vote v left join t_vote_user vu on vu.vote_id = v.id
where vu.user_id = :user_id and status = 1 and (end_time > now() or end_time is null)
and v.id not in (select vote_id from t_vote_answer where user_id = :user_id)

-- :name list-my_done_vote :? :* :D
select v.* from t_vote v left join t_vote_user vu on vu.vote_id = v.id
where vu.user_id = :user_id and v.id in (select vote_id from t_vote_answer where user_id = :user_id)
order by v.id
--~ [limit :limit offset :offset]

-- :name list-my_done_vote-count :? :1
select count(1) as cnt from t_vote v left join t_vote_user vu on vu.vote_id = v.id
where vu.user_id = :user_id and v.id in (select vote_id from t_vote_answer where user_id = :user_id)

--:name list-vote_answer_user-count :? :1
select count(distinct(user_id)) as cnt from t_vote_answer where vote_id = :vote_id

--:name list-vote_user-count :? :1
select count(user_id) as cnt from t_vote_user where vote_id = :vote_id

--:name list-vote_answer :? :* :D
select * from t_vote_answer
--~ where vote_id = :vote_id [and user_id = :user_id]

--:name list-vote_instance :? :* :D
select distinct(va.user_id), u.nick_name, u.user_name, u.`number`, va.create_time from t_vote_answer va
left join sys_user u on va.user_id = u.user_id
--~ where va.vote_id = :vote_id [and u.nick_name like :l:nick_name] [and u.`number` like :l:number]
order by va.create_time
--~ [limit :limit offset :offset]

--:name list-vote_instance-count :? :1 :D
select count(distinct(va.user_id)) as cnt from t_vote_answer va
left join sys_user u on va.user_id = u.user_id
--~ where va.vote_id = :vote_id [and u.nick_name like :l:nick_name] [and u.`number` like :l:number]
