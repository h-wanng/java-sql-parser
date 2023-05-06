/* sy_news */
-- :name list-sy_news :? :* :D
select sn.id, sn.news_name, sn.dept_id, sd.dept_name, sn.keywords, convert(sn.content using utf8mb4) as content, sn.status, sn.appendix,
sn.`type`, sn.del_flag, sn.publish_time, sn.create_by, sn.create_time, sn.update_by, sn.update_time, sn.remarks
from sy_news sn
left join sys_dept sd on sn.dept_id = sd.dept_id
/*~ where sn.del_flag = '0' [and sn.id = :news_id] [and sn.`type` = :type] [and sn.news_name like :l:news_name]
[and sn.status = :status] [and sn.dept_id = :dept_id] [and sn.dept_id = (select dept_id from sys_user where user_id = :user_id)]
[and date_format(sn.create_time, '%Y-%m-%d') >= :beginTime] [and date_format(sn.create_time, '%Y-%m-%d') <= :endTime]
~*/
order by sn.create_time desc
--~ [limit :limit offset :offset]

-- :name list-sy_news-count :? :1 :D
select count(1) as cnt from sy_news sn
/*~ where sn.del_flag = '0' [and sn.id = :news_id] [and sn.`type` = :type] [and sn.news_name like :l:news_name]
[and sn.status = :status] [and sn.dept_id = :dept_id] [and sn.dept_id = (select dept_id from sys_user where user_id = :user_id)]
[and date_format(sn.create_time, '%Y-%m-%d') >= :beginTime] [and date_format(sn.create_time, '%Y-%m-%d') <= :endTime]
~*/

-- :name get-detail-sy_news :? :1
select sn.news_name, sn.publish_time, sn.create_time, sd.dept_name, sn.status, convert(sn.content using utf8mb4) as content, sn.keywords, sn.appendix
from sy_news sn left join sys_dept sd on sn.dept_id = sd.dept_id
where id = :id

-- :name finish-user-project :! :n
update tr_training_project_user set completed = '3' where project_id = :project_id and user_id = :user_id
